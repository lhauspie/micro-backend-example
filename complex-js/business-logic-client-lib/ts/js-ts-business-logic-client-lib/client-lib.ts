import axios, { AxiosResponse } from 'axios';

class ClientLibrary {
    private providerUrl: string;
    private cacheDurationInSeconds: number;
    private cacheTimestampInSeconds: number = 0;
    private businessLogicScript: string | null = null;
    private lastModified: string | null = null;

    constructor(providerUrl: string, cacheDurationInSeconds: number) {
        this.providerUrl = providerUrl;
        this.cacheDurationInSeconds = cacheDurationInSeconds;
    }

    private isCacheValid(): boolean {
        const currentTimestampInSeconds = Math.floor(Date.now() / 1000);
        return (currentTimestampInSeconds - this.cacheTimestampInSeconds) < this.cacheDurationInSeconds;
    }

    public async fetchBusinessLogic(): Promise<string | null> {
        const headers: any = {};

        if (this.isCacheValid()) {
            return this.businessLogicScript;
        }

        if (this.lastModified) {
            headers['If-Modified-Since'] = this.lastModified;
        }

        try {
            const validateStatus = (status: number) => {
                return status >= 200 && status < 400;
            };
            const response: AxiosResponse<string> = await axios.get<string>(this.providerUrl + "/business-logic.js", { headers, validateStatus });
            if (response.status === 200) {
                this.cache(response);
                return response.data;
            } else if (response.status === 304) {
                this.cache();
                return this.businessLogicScript;
            }
        } catch (error: any) {
            if (this.businessLogicScript) {
                return this.businessLogicScript;
            }
            throw new Error(`Failed to fetch business logic and no cache available : ${error.message}`);
        }

        return null;
    }

    private cache(response?: AxiosResponse<string>): void {
        if (response !== undefined) {
            this.businessLogicScript = response.data;
            this.lastModified = response.headers['last-modified'];
        }
        this.cacheTimestampInSeconds = Math.floor(Date.now() / 1000);
    }

    public async executeBusinessLogic(object: any): Promise<any> {
        try {
            const script = await this.fetchBusinessLogic();
            if (script) {
                const businessLogic = new Function('module', 'exports', script);
                const module: any = { exports: {} };
                businessLogic(module, module.exports);
                return module.exports.execute(object);
            } else {
                throw new Error('Failed to load business logic script');
            }
        } catch (error: any) {
            return {
                errors: [{
                    code: 'CLIENT_LIB',
                    message: error.message
                }]
            };
        }
    }
}

export default ClientLibrary;
