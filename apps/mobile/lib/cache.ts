import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

export interface TokenCache {
  getToken: (key: string) => Promise<string | null>;
  saveToken: (key: string, value: string) => Promise<void>;
  clearToken?: (key: string) => Promise<void>;
}

const createTokenCache = (): TokenCache => {
  return {
    async getToken(key: string) {
      try {
        const item = await SecureStore.getItemAsync(key);
        return item;
      } catch (error) {
        return null;
      }
    },
    async saveToken(key: string, value: string) {
      try {
        await SecureStore.setItemAsync(key, value);
      } catch (err) {
        return;
      }
    },
    async clearToken(key: string) {
      try {
        await SecureStore.deleteItemAsync(key);
      } catch (err) {
        return;
      }
    },
  };
};

// SecureStore is supported on iOS and Android native platforms; undefined on web
export const tokenCache = Platform.OS !== 'web' ? createTokenCache() : undefined;
