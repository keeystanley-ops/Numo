import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  Image,
  ScrollView,
  SafeAreaView,
  StatusBar,
  Platform,
  KeyboardAvoidingView,
  ActivityIndicator,
} from 'react-native';
import { useRouter } from 'expo-router';
import Svg, { Path } from 'react-native-svg';
import { useSignIn, useOAuth } from '@clerk/expo';
import * as WebBrowser from 'expo-web-browser';
import * as Linking from 'expo-linking';

// Ensure browser auth redirect is completed
WebBrowser.maybeCompleteAuthSession();

export default function SignInScreen() {
  const router = useRouter();
  const { signIn, setActive, isLoaded: isSignInLoaded } = useSignIn();
  const { startOAuthFlow: startGoogleOAuth } = useOAuth({ strategy: 'oauth_google' });
  const { startOAuthFlow: startAppleOAuth } = useOAuth({ strategy: 'oauth_apple' });

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Google OAuth Handler
  const onGoogleSignInPress = useCallback(async () => {
    try {
      setLoading(true);
      setErrorMsg(null);
      const redirectUrl = Linking.createURL('/(tabs)/money', { scheme: 'numo' });
      const { createdSessionId, setActive: setOAuthActive } = await startGoogleOAuth({ redirectUrl });
      if (createdSessionId && setOAuthActive) {
        await setOAuthActive({ session: createdSessionId });
        router.replace('/(tabs)/money' as any);
      }
    } catch (err: any) {
      console.log('Google OAuth error:', err);
      if (err?.errors?.[0]?.code !== 'oauth_cancelled') {
        setErrorMsg(err?.errors?.[0]?.message || 'Google sign-in failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }, [startGoogleOAuth, router]);

  // Apple OAuth Handler
  const onAppleSignInPress = useCallback(async () => {
    try {
      setLoading(true);
      setErrorMsg(null);
      const redirectUrl = Linking.createURL('/(tabs)/money', { scheme: 'numo' });
      const { createdSessionId, setActive: setOAuthActive } = await startAppleOAuth({ redirectUrl });
      if (createdSessionId && setOAuthActive) {
        await setOAuthActive({ session: createdSessionId });
        router.replace('/(tabs)/money' as any);
      }
    } catch (err: any) {
      console.log('Apple OAuth error:', err);
      if (err?.errors?.[0]?.code !== 'oauth_cancelled') {
        setErrorMsg(err?.errors?.[0]?.message || 'Apple sign-in failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }, [startAppleOAuth, router]);

  // Standard Email/Password Sign In
  const onSignInPress = useCallback(async () => {
    if (!isSignInLoaded || !signIn) {
      // In preview mode or before Clerk loads, allow direct navigation
      router.replace('/');
      return;
    }
    if (!email.trim() || !password) {
      setErrorMsg('Please enter both your email address and password.');
      return;
    }
    try {
      setLoading(true);
      setErrorMsg(null);
      const result = await signIn.create({
        identifier: email.trim(),
        password,
      });
      if (result.status === 'complete' && result.createdSessionId) {
        await setActive({ session: result.createdSessionId });
        router.replace('/');
      } else {
        setErrorMsg(`Sign in incomplete: ${result.status}`);
      }
    } catch (err: any) {
      setErrorMsg(err?.errors?.[0]?.message || 'Unable to sign in. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  }, [isSignInLoaded, signIn, email, password, setActive, router]);

  return (
    <SafeAreaView className="flex-1 bg-[#D4FAEF]">
      <StatusBar barStyle="dark-content" backgroundColor="#D4FAEF" />
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        className="flex-1"
      >
        <ScrollView
          contentContainerStyle={{ flexGrow: 1 }}
          showsVerticalScrollIndicator={false}
          bounces={false}
        >
          {/* Top Mint Section */}
          <View className="items-center pt-4 pb-2 px-6">
            {/* 1. Brand Logo */}
            <Image
              source={require('../../assets/auth_image_hero_1.png')}
              style={{ width: 62, height: 50, resizeMode: 'contain' }}
              className="mb-2"
            />

            {/* 2. Brand Title */}
            <Text className="text-[34px] font-bold text-[#001A49] tracking-tight">
              Numo
            </Text>

            {/* 3. Tagline */}
            <Text className="text-[19px] font-semibold text-[#0F2F4A] text-center mt-1 leading-7">
              Smarter Money.{'\n'}Brighter Future.
            </Text>

            {/* 4. 3D Wallet & Coins Hero Illustration */}
            <Image
              source={require('../../assets/wallet_hero.png')}
              style={{ width: '100%', height: 175, resizeMode: 'contain' }}
              className="mt-2"
            />
          </View>

          {/* Bottom Floating White Card */}
          <View className="flex-1 bg-white rounded-t-[36px] px-7 pt-8 pb-10 shadow-2xl">
            {/* Header */}
            <Text className="text-[26px] font-bold text-[#001A49] text-center">
              Welcome Back
            </Text>
            <Text className="text-[15px] font-semibold text-[#334E68] text-center mt-1.5 leading-5">
              Sign in to continue your{'\n'}financial journey
            </Text>

            {/* Error Message Banner */}
            {errorMsg && (
              <View className="bg-red-50 border border-red-200 rounded-xl p-3 mt-4">
                <Text className="text-red-700 text-[13px] text-center font-medium">
                  {errorMsg}
                </Text>
              </View>
            )}

            {/* Form Fields */}
            <View className="mt-5 space-y-4">
              {/* Email Input */}
              <View className="flex-row items-center bg-white border border-[#E2E8F0] rounded-full h-[54px] px-5 mb-3.5">
                <Svg width={20} height={20} viewBox="0 0 24 24" fill="none" stroke="#94A3B8" strokeWidth={1.8}>
                  <Path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                  <Path d="M22 6l-10 7L2 6" />
                </Svg>
                <TextInput
                  value={email}
                  onChangeText={(t) => {
                    setEmail(t);
                    if (errorMsg) setErrorMsg(null);
                  }}
                  placeholder="Email address"
                  placeholderTextColor="#94A3B8"
                  keyboardType="email-address"
                  autoCapitalize="none"
                  className="flex-1 ml-3 text-[15px] font-medium text-[#001A49]"
                />
              </View>

              {/* Password Input */}
              <View className="flex-row items-center bg-white border border-[#E2E8F0] rounded-full h-[54px] px-5 mb-5">
                <Svg width={20} height={20} viewBox="0 0 24 24" fill="none" stroke="#94A3B8" strokeWidth={1.8}>
                  <Path d="M19 11H5c-1.1 0-2 .9-2 2v7c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-7c0-1.1-.9-2-2-2z" />
                  <Path d="M7 11V7a5 5 0 0110 0v4" />
                </Svg>
                <TextInput
                  value={password}
                  onChangeText={(t) => {
                    setPassword(t);
                    if (errorMsg) setErrorMsg(null);
                  }}
                  placeholder="Password"
                  placeholderTextColor="#94A3B8"
                  secureTextEntry={!showPassword}
                  className="flex-1 ml-3 text-[15px] font-medium text-[#001A49]"
                />
                <TouchableOpacity onPress={() => setShowPassword(!showPassword)}>
                  <Svg width={20} height={20} viewBox="0 0 24 24" fill="none" stroke="#94A3B8" strokeWidth={1.8}>
                    <Path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8z" />
                    <Path d="M12 9a3 3 0 100 6 3 3 0 000-6z" />
                  </Svg>
                </TouchableOpacity>
              </View>

              {/* Primary Sign In Button with back shadow */}
              <TouchableOpacity
                activeOpacity={0.85}
                onPress={onSignInPress}
                disabled={loading}
                style={{
                  shadowColor: '#027858',
                  shadowOffset: { width: 0, height: 6 },
                  shadowOpacity: 0.38,
                  shadowRadius: 10,
                  elevation: 8,
                }}
                className={`bg-[#029766] h-[54px] rounded-full items-center justify-center flex-row ${
                  loading ? 'opacity-80' : ''
                }`}
              >
                {loading ? (
                  <ActivityIndicator color="#FFFFFF" />
                ) : (
                  <>
                    <Text className="text-white text-[16px] font-bold tracking-wide">
                      Sign In
                    </Text>
                    <Text className="text-white text-[18px] font-bold ml-2">
                      →
                    </Text>
                  </>
                )}
              </TouchableOpacity>

              {/* "Or" Divider */}
              <View className="flex-row items-center my-4">
                <View className="flex-1 h-[1px] bg-[#E2E8F0]" />
                <Text className="mx-3.5 text-[#627D98] text-[13px] font-medium">
                  Or
                </Text>
                <View className="flex-1 h-[1px] bg-[#E2E8F0]" />
              </View>

              {/* Social Buttons: Google & Apple */}
              <View className="flex-row justify-between space-x-3.5">
                {/* Google Button */}
                <TouchableOpacity
                  activeOpacity={0.8}
                  onPress={onGoogleSignInPress}
                  disabled={loading}
                  className="flex-1 h-[50px] rounded-full border border-[#E2E8F0] items-center justify-center bg-white mr-2"
                >
                  <Svg width={22} height={22} viewBox="0 0 24 24">
                    <Path
                      fill="#4285F4"
                      d="M23.745 12.27c0-.71-.06-1.4-.18-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.665-5.17 3.665-9.17z"
                    />
                    <Path
                      fill="#34A853"
                      d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.93H1.24v3.13C3.26 21.36 7.33 24 12 24z"
                    />
                    <Path
                      fill="#FBBC05"
                      d="M5.28 14.27c-.25-.72-.38-1.49-.38-2.27s.14-1.55.38-2.27V6.6H1.24C.45 8.18 0 9.98 0 12s.45 3.82 1.24 5.4l4.04-3.13z"
                    />
                    <Path
                      fill="#EA4335"
                      d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.33 0 3.26 2.64 1.24 6.6l4.04 3.13c.95-2.83 3.6-4.98 6.72-4.98z"
                    />
                  </Svg>
                </TouchableOpacity>

                {/* Apple Button */}
                <TouchableOpacity
                  activeOpacity={0.8}
                  onPress={onAppleSignInPress}
                  disabled={loading}
                  className="flex-1 h-[50px] rounded-full border border-[#E2E8F0] items-center justify-center bg-white ml-2"
                >
                  <Svg width={22} height={22} viewBox="0 0 24 24" fill="#000000">
                    <Path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M15.97 5.88c.66-.8 1.11-1.92.99-3.03-.95.04-2.1.63-2.78 1.43-.6.7-1.13 1.83-.99 2.92 1.06.08 2.14-.55 2.78-1.32z" />
                  </Svg>
                </TouchableOpacity>
              </View>

              {/* Sign Up Link */}
              <View className="flex-row justify-center mt-5">
                <Text className="text-[#486581] font-medium text-[14px]">
                  Don't have an account?{' '}
                </Text>
                <TouchableOpacity onPress={() => router.push('/(auth)/sign-up' as any)}>
                  <Text className="text-[#029766] text-[14px] font-bold">
                    Sign Up
                  </Text>
                </TouchableOpacity>
              </View>
            </View>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
