const axios = require('axios');

class ClientLibrary {
    constructor(providerUrl, cacheDurationInSeconds) {
        this.providerUrl = providerUrl;
        this.cacheDurationInSeconds = cacheDurationInSeconds;
        this.cacheTimestampInSeconds = 0;
        this.businessLogicScript = null;
        this.lastModified = null;
    }

    isCacheValid() {
        const currentTimestampInSeconds = Math.floor(Date.now() / 1000);
        return (currentTimestampInSeconds - this.cacheTimestampInSeconds) < this.cacheDurationInSeconds;
    }

    async fetchBusinessLogic() {
        const headers = {};

        if (this.isCacheValid()) {
            return this.businessLogicScript;
        }

        if (this.lastModified) {
            headers['If-Modified-Since'] = this.lastModified;
        }

        try {
            const validateStatus = function(status) {return status >= 200 && status < 400};
            const response = await axios.get(this.providerUrl + "/business-logic.js", { headers, validateStatus });
            if (response.status === 200) {
                this.cache(response);
                this.cacheTimestampInSeconds = Math.floor(Date.now() / 1000);
                return response.data;
            } else if (response.status === 304) {
                this.cacheTimestampInSeconds = Math.floor(Date.now() / 1000);
                return this.businessLogicScript;
            }
        } catch (error) {
            if (this.businessLogicScript) {
                return this.businessLogicScript;
            }
            throw new Error('Failed to fetch business logic and no cache available');
        }
    }

    cache(response) {
        this.businessLogicScript = response.data;
        this.lastModified = response.headers['last-modified'];
    }

    async executeBusinessLogic(object) {
        const script = await this.fetchBusinessLogic();
        const businessLogic = new Function('module', 'exports', script);
        const module = { exports: {} };
        businessLogic(module, module.exports);
        return module.exports.execute(object);
    }
}

module.exports = ClientLibrary;
