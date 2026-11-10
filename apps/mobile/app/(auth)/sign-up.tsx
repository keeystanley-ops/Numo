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
import { useSignUp, useOAuth } from '@clerk/expo';
import * as WebBrowser from 'expo-web-browser';
import * as Linking from 'expo-linking';

WebBrowser.maybeCompleteAuthSession();

export default function SignUpScreen() {
  const router = useRouter();
  const { signUp, setActive, isLoaded } = useSignUp();
  const { startOAuthFlow: startGoogleOAuth } = useOAuth({ strategy: 'oauth_google' });
  const { startOAuthFlow: startAppleOAuth } = useOAuth({ strategy: 'oauth_apple' });

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [pendingVerification, setPendingVerification] = useState(false);
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // 1. Submit Sign Up
  const onSignUpPress = useCallback(async () => {
    if (!isLoaded || !signUp) return;

    // Form Validations
    if (!fullName.trim()) {
      setErrorMessage('Please enter your full name.');
      return;
    }
    if (!email.trim() || !email.includes('@')) {
      setErrorMessage('Please enter a valid email address.');
      return;
    }
    if (password.length < 8) {
      setErrorMessage('Password must be at least 8 characters long.');
      return;
    }
    if (password !== confirmPassword) {
      setErrorMessage('Passwords do not match. Please verify.');
      return;
    }

    setLoading(true);
    setErrorMessage(null);

    try {
      const names = fullName.trim().split(' ', 2);
      await signUp.create({
        emailAddress: email.trim(),
        password,
        firstName: names[0] || undefined,
        lastName: names[1] || undefined,
      });

      // Send verification email
      await signUp.prepareEmailAddressVerification({ strategy: 'email_code' });
      setPendingVerification(true);
    } catch (err: any) {
      setErrorMessage(err?.errors?.[0]?.message || 'Failed to create account.');
    } finally {
      setLoading(false);
    }
  }, [isLoaded, signUp, fullName, email, password, confirmPassword]);

  // 2. Verify Email Code
  const onVerifyPress = useCallback(async () => {
    if (!isLoaded || !signUp) return;
    if (code.length < 6) {
      setErrorMessage('Please enter the complete 6-digit code.');
      return;
    }

    setLoading(true);
    setErrorMessage(null);

    try {
      const completeSignUp = await signUp.attemptEmailAddressVerification({ code: code.trim() });
      if (completeSignUp.status === 'complete' && completeSignUp.createdSessionId) {
        await setActive({ session: completeSignUp.createdSessionId });
        router.replace('/');
      } else {
        setErrorMessage(`Verification status: ${completeSignUp.status}`);
      }
    } catch (err: any) {
      setErrorMessage(err?.errors?.[0]?.message || 'Verification failed. Please check the code.');
    } finally {
      setLoading(false);
    }
  }, [isLoaded, signUp, code, setActive, router]);

  // 3. Google OAuth
  const onGoogleSignUp = useCallback(async () => {
    try {
      setLoading(true);
      setErrorMessage(null);
      const redirectUrl = Linking.createURL('/(tabs)/money', { scheme: 'numo' });
      const { createdSessionId, setActive: setOAuthActive } = await startGoogleOAuth({ redirectUrl });
      if (createdSessionId && setOAuthActive) {
        await setOAuthActive({ session: createdSessionId });
        router.replace('/(tabs)/money' as any);
      }
    } catch (err: any) {
      if (err?.errors?.[0]?.code !== 'oauth_cancelled') {
        setErrorMessage(err?.errors?.[0]?.message || 'Google sign-up failed.');
      }
    } finally {
      setLoading(false);
    }
  }, [startGoogleOAuth, router]);

  // 4. Apple OAuth
  const onAppleSignUp = useCallback(async () => {
    try {
      setLoading(true);
      setErrorMessage(null);
      const redirectUrl = Linking.createURL('/(tabs)/money', { scheme: 'numo' });
      const { createdSessionId, setActive: setOAuthActive } = await startAppleOAuth({ redirectUrl });
      if (createdSessionId && setOAuthActive) {
        await setOAuthActive({ session: createdSessionId });
        router.replace('/(tabs)/money' as any);
      }
    } catch (err: any) {
      if (err?.errors?.[0]?.code !== 'oauth_cancelled') {
        setErrorMessage(err?.errors?.[0]?.message || 'Apple sign-up failed.');
      }
    } finally {
      setLoading(false);
    }
  }, [startAppleOAuth, router]);

  return (
    <SafeAreaView className="flex-1 bg-[#D2FCEF]">
      <StatusBar barStyle="dark-content" backgroundColor="#D2FCEF" />
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        className="flex-1"
      >
        <ScrollView
          contentContainerStyle={{ flexGrow: 1 }}
          showsVerticalScrollIndicator={false}
          className="px-6 py-6"
        >
          {/* Header */}
          <View className="items-center mb-5">
            <Image
              source={require('../../assets/auth_image_hero_1.png')}
              style={{ width: 66, height: 52, resizeMode: 'contain' }}
              className="mb-1"
            />
            <Text className="text-[34px] font-bold text-[#001A49] tracking-tight">
              Numo
            </Text>

            {!pendingVerification ? (
              <>
                <Text className="text-[26px] font-bold text-[#001A49] text-center mt-2">
                  Create Your <Text className="text-[#029766]">Account</Text>
                </Text>
                <Text className="text-[15px] font-semibold text-[#334E68] text-center mt-1 leading-5">
                  Take control of your finances and{'\n'}build a brighter future.
                </Text>
              </>
            ) : (
              <>
                <Text className="text-[26px] font-bold text-[#001A49] text-center mt-2">
                  Verify Your Email
                </Text>
                <Text className="text-[15px] font-semibold text-[#334E68] text-center mt-1 leading-5">
                  Enter the 6-digit code sent to{'\n'}{email}
                </Text>
              </>
            )}
          </View>

          {/* Error Banner */}
          {errorMessage && (
            <View className="bg-red-50 border border-red-200 rounded-2xl p-3.5 mb-4">
              <Text className="text-red-700 text-[13px] text-center font-medium">
                {errorMessage}
              </Text>
            </View>
          )}

          {!pendingVerification ? (
            /* Sign Up Form */
            <View className="space-y-3.5">
              {/* Full Name */}
              <View className="flex-row items-center bg-white border border-[#E2E8F0] rounded-full h-[54px] px-5 mb-3.5">
                <Svg width={20} height={20} viewBox="0 0 24 24" fill="none" stroke="#029766" strokeWidth={1.8}>
                  <Path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
                  <Path d="M12 11a4 4 0 100-8 4 4 0 000 8z" />
                </Svg>
                <TextInput
                  value={fullName}
                  onChangeText={(t) => { setFullName(t); setErrorMessage(null); }}
                  placeholder="Full Name"
                  placeholderTextColor="#94A3B8"
                  className="flex-1 ml-3 text-[15px] font-medium text-[#001A49]"
                />
              </View>

              {/* Email */}
              <View className="flex-row items-center bg-white border border-[#E2E8F0] rounded-full h-[54px] px-5 mb-3.5">
                <Svg width={20} height={20} viewBox="0 0 24 24" fill="none" stroke="#029766" strokeWidth={1.8}>
                  <Path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                  <Path d="M22 6l-10 7L2 6" />
                </Svg>
                <TextInput
                  value={email}
                  onChangeText={(t) => { setEmail(t); setErrorMessage(null); }}
                  placeholder="Email Address"
                  placeholderTextColor="#94A3B8"
                  keyboardType="email-address"
                  autoCapitalize="none"
                  className="flex-1 ml-3 text-[15px] font-medium text-[#001A49]"
                />
              </View>

              {/* Password */}
              <View className="flex-row items-center bg-white border border-[#E2E8F0] rounded-full h-[54px] px-5 mb-3.5">
                <Svg width={20} height={20} viewBox="0 0 24 24" fill="none" stroke="#029766" strokeWidth={1.8}>
                  <Path d="M19 11H5c-1.1 0-2 .9-2 2v7c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-7c0-1.1-.9-2-2-2z" />
                  <Path d="M7 11V7a5 5 0 0110 0v4" />
                </Svg>
                <TextInput
                  value={password}
                  onChangeText={(t) => { setPassword(t); setErrorMessage(null); }}
                  placeholder="Password"
                  placeholderTextColor="#94A3B8"
                  secureTextEntry={!showPassword}
                  className="flex-1 ml-3 text-[15px] font-medium text-[#001A49]"
                />
                <TouchableOpacity onPress={() => setShowPassword(!showPassword)}>
                  <Svg width={20} height={20} viewBox="0 0 24 24" fill="none" stroke="#94A3B8" strokeWidth={1.8}>
                    <Path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <Path d="M12 9a3 3 0 100 6 3 3 0 000-6z" />
                  </Svg>
                </TouchableOpacity>
              </View>

              {/* Confirm Password */}
              <View className="flex-row items-center bg-white border border-[#E2E8F0] rounded-full h-[54px] px-5 mb-5">
                <Svg width={20} height={20} viewBox="0 0 24 24" fill="none" stroke="#029766" strokeWidth={1.8}>
                  <Path d="M19 11H5c-1.1 0-2 .9-2 2v7c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-7c0-1.1-.9-2-2-2z" />
                  <Path d="M7 11V7a5 5 0 0110 0v4" />
                </Svg>
                <TextInput
                  value={confirmPassword}
                  onChangeText={(t) => { setConfirmPassword(t); setErrorMessage(null); }}
                  placeholder="Confirm Password"
                  placeholderTextColor="#94A3B8"
                  secureTextEntry={!showConfirmPassword}
                  className="flex-1 ml-3 text-[15px] font-medium text-[#001A49]"
                />
                <TouchableOpacity onPress={() => setShowConfirmPassword(!showConfirmPassword)}>
                  <Svg width={20} height={20} viewBox="0 0 24 24" fill="none" stroke="#94A3B8" strokeWidth={1.8}>
                    <Path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <Path d="M12 9a3 3 0 100 6 3 3 0 000-6z" />
                  </Svg>
                </TouchableOpacity>
              </View>

              {/* Primary Create Account Button */}
              <TouchableOpacity
                activeOpacity={0.85}
                onPress={onSignUpPress}
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
                      Create Account
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

              {/* Full-width Google Sign Up */}
              <TouchableOpacity
                activeOpacity={0.8}
                onPress={onGoogleSignUp}
                disabled={loading}
                className="w-full h-[52px] rounded-full border border-[#E2E8F0] items-center justify-center bg-white flex-row mb-3"
              >
                <Svg width={22} height={22} viewBox="0 0 24 24">
                  <Path fill="#4285F4" d="M23.745 12.27c0-.71-.06-1.4-.18-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.665-5.17 3.665-9.17z" />
                  <Path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.93H1.24v3.13C3.26 21.36 7.33 24 12 24z" />
                  <Path fill="#FBBC05" d="M5.28 14.27c-.25-.72-.38-1.49-.38-2.27s.14-1.55.38-2.27V6.6H1.24C.45 8.18 0 9.98 0 12s.45 3.82 1.24 5.4l4.04-3.13z" />
                  <Path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.33 0 3.26 2.64 1.24 6.6l4.04 3.13c.95-2.83 3.6-4.98 6.72-4.98z" />
                </Svg>
                <Text className="text-[#001A49] text-[15px] font-semibold ml-3">
                  Sign up with Google
                </Text>
              </TouchableOpacity>

              {/* Full-width Apple Sign Up */}
              <TouchableOpacity
                activeOpacity={0.8}
                onPress={onAppleSignUp}
                disabled={loading}
                className="w-full h-[52px] rounded-full border border-[#E2E8F0] items-center justify-center bg-white flex-row mb-4"
              >
                <Svg width={22} height={22} viewBox="0 0 24 24" fill="#000000">
                  <Path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M15.97 5.88c.66-.8 1.11-1.92.99-3.03-.95.04-2.1.63-2.78 1.43-.6.7-1.13 1.83-.99 2.92 1.06.08 2.14-.55 2.78-1.32z" />
                </Svg>
                <Text className="text-[#001A49] text-[15px] font-semibold ml-3">
                  Sign up with Apple
                </Text>
              </TouchableOpacity>

              {/* Footer: Already have an account? Sign In */}
              <View className="flex-row justify-center mt-2 mb-4">
                <Text className="text-[#486581] font-medium text-[14px]">
                  Already have an account?{' '}
                </Text>
                <TouchableOpacity onPress={() => router.push('/(auth)/sign-in' as any)}>
                  <Text className="text-[#029766] text-[14px] font-bold">
                    Sign In
                  </Text>
                </TouchableOpacity>
              </View>

              {/* Terms Badge */}
              <View className="flex-row items-center justify-center px-4">
                <Svg width={18} height={18} viewBox="0 0 24 24" fill="none" stroke="#029766" strokeWidth={2}>
                  <Path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                </Svg>
                <Text className="text-[#627D98] text-[11px] leading-4 ml-2 text-center">
                  By creating an account, you agree to our Terms of Service and Privacy Policy.
                </Text>
              </View>
            </View>
          ) : (
            /* Email Code Verification View */
            <View className="space-y-4 mt-2">
              <View className="bg-white border border-[#029766] rounded-2xl h-[56px] px-5 mb-4 justify-center">
                <TextInput
                  value={code}
                  onChangeText={(t) => { setCode(t); setErrorMessage(null); }}
                  placeholder="Enter 6-digit code"
                  placeholderTextColor="#94A3B8"
                  keyboardType="numeric"
                  maxLength={6}
                  className="text-center text-[22px] font-bold tracking-[8px] text-[#001A49]"
                />
              </View>

              <TouchableOpacity
                activeOpacity={0.85}
                onPress={onVerifyPress}
                disabled={loading}
                className="bg-[#029766] h-[54px] rounded-full items-center justify-center flex-row shadow-md"
              >
                {loading ? (
                  <ActivityIndicator color="#FFFFFF" />
                ) : (
                  <>
                    <Text className="text-white text-[16px] font-bold">
                      Verify & Complete
                    </Text>
                    <Text className="text-white text-[18px] font-bold ml-2">
                      →
                    </Text>
                  </>
                )}
              </TouchableOpacity>

              <TouchableOpacity
                onPress={() => setPendingVerification(false)}
                className="items-center mt-3"
              >
                <Text className="text-[#627D98] text-[14px] font-medium">
                  ← Back to details
                </Text>
              </TouchableOpacity>
            </View>
          )}
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
