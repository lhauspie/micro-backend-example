"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const axios_1 = __importDefault(require("axios"));
class ClientLibrary {
    constructor(providerUrl, cacheDurationInSeconds) {
        this.cacheTimestampInSeconds = 0;
        this.businessLogicScript = null;
        this.lastModified = null;
        this.providerUrl = providerUrl;
        this.cacheDurationInSeconds = cacheDurationInSeconds;
    }
    isCacheValid() {
        const currentTimestampInSeconds = Math.floor(Date.now() / 1000);
        return (currentTimestampInSeconds - this.cacheTimestampInSeconds) < this.cacheDurationInSeconds;
    }
    fetchBusinessLogic() {
        return __awaiter(this, void 0, void 0, function* () {
            const headers = {};
            if (this.isCacheValid()) {
                return this.businessLogicScript;
            }
            if (this.lastModified) {
                headers['If-Modified-Since'] = this.lastModified;
            }
            try {
                const validateStatus = (status) => {
                    return status >= 200 && status < 400;
                };
                const response = yield axios_1.default.get(this.providerUrl + "/business-logic.js", { headers, validateStatus });
                if (response.status === 200) {
                    this.cache(response);
                    return response.data;
                }
                else if (response.status === 304) {
                    this.cache();
                    return this.businessLogicScript;
                }
            }
            catch (error) {
                if (this.businessLogicScript) {
                    return this.businessLogicScript;
                }
                throw new Error(`Failed to fetch business logic and no cache available : ${error.message}`);
            }
            return null;
        });
    }
    cache(response) {
        if (response !== undefined) {
            this.businessLogicScript = response.data;
            this.lastModified = response.headers['last-modified'];
        }
        this.cacheTimestampInSeconds = Math.floor(Date.now() / 1000);
    }
    executeBusinessLogic(object) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const script = yield this.fetchBusinessLogic();
                if (script) {
                    const businessLogic = new Function('module', 'exports', script);
                    const module = { exports: {} };
                    businessLogic(module, module.exports);
                    return module.exports.execute(object);
                }
                else {
                    throw new Error('Failed to load business logic script');
                }
            }
            catch (error) {
                return {
                    errors: [{
                            code: 'CLIENT_LIB',
                            message: error.message
                        }]
                };
            }
        });
    }
}
exports.default = ClientLibrary;
