const SecureKey = Capacitor.registerPlugin('SecureKey');

const SecureStorage = {
    async saveApiKey(provider, apiKey) {
        return SecureKey.saveKey({ provider, apiKey });
    },

    async getApiKey(provider) {
        const result = await SecureKey.getKey({ provider });
        return result.apiKey;
    },

    async deleteApiKey(provider) {
        return SecureKey.deleteKey({ provider });
    }
};

window.SecureStorage = SecureStorage;
