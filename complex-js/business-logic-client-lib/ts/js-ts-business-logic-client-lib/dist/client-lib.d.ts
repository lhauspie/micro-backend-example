declare class ClientLibrary {
    private providerUrl;
    private cacheDurationInSeconds;
    private cacheTimestampInSeconds;
    private businessLogicScript;
    private lastModified;
    constructor(providerUrl: string, cacheDurationInSeconds: number);
    private isCacheValid;
    fetchBusinessLogic(): Promise<string | null>;
    private cache;
    executeBusinessLogic(object: any): Promise<any>;
}
export default ClientLibrary;
