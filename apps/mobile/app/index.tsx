import { SignedIn, SignedOut, useUser, useClerk } from '@clerk/expo';
import { useRouter } from 'expo-router';
import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  Platform,
} from 'react-native';

export default function Index() {
  const { user } = useUser();
  const { signOut } = useClerk();
  const router = useRouter();

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Brand Header */}
        <View style={styles.brandHeader}>
          <View style={styles.logoBadge}>
            <Text style={styles.logoText}>N</Text>
          </View>
          <Text style={styles.appName}>Numo</Text>
          <Text style={styles.appTagline}>
            Personal Finance & Wealth Operating System
          </Text>
        </View>

        {/* Signed In State */}
        <SignedIn>
          <View style={styles.card}>
            <View style={styles.userHeader}>
              <View style={styles.avatar}>
                <Text style={styles.avatarText}>
                  {user?.firstName ? user.firstName[0] : (user?.primaryEmailAddress?.emailAddress[0]?.toUpperCase() ?? 'U')}
                </Text>
              </View>
              <View style={styles.userInfo}>
                <Text style={styles.welcomeText}>Welcome back,</Text>
                <Text style={styles.userName}>
                  {user?.fullName || user?.primaryEmailAddress?.emailAddress || 'User'}
                </Text>
              </View>
            </View>

            <View style={styles.divider} />

            <View style={styles.environmentGrid}>
              <TouchableOpacity
                style={[styles.envButton, styles.moneyEnvButton]}
                onPress={() => router.push('/(tabs)/money' as any)}
                activeOpacity={0.8}
              >
                <Text style={styles.envButtonBadge}>Daily Control</Text>
                <Text style={styles.envButtonTitle}>Money Management</Text>
                <Text style={styles.envButtonDesc}>
                  Cash, M-PESA, Budgets, Bills, Debt
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.envButton, styles.investEnvButton]}
                onPress={() => router.push('/(tabs)/investments' as any)}
                activeOpacity={0.8}
              >
                <Text style={[styles.envButtonBadge, styles.investBadge]}>
                  Wealth Growth
                </Text>
                <Text style={styles.envButtonTitle}>Investment Management</Text>
                <Text style={styles.envButtonDesc}>
                  NSE, MMF, Bonds, SACCO, Dividends
                </Text>
              </TouchableOpacity>
            </View>

            <TouchableOpacity
              style={styles.signOutButton}
              onPress={() => signOut()}
              activeOpacity={0.8}
            >
              <Text style={styles.signOutText}>Sign Out</Text>
            </TouchableOpacity>
          </View>
        </SignedIn>

        {/* Signed Out State */}
        <SignedOut>
          <View style={styles.card}>
            <Text style={styles.cardTitle}>Sign in to Numo</Text>
            <Text style={styles.cardSubtitle}>
              Two distinct environments, one unified financial foundation.
            </Text>

            <TouchableOpacity
              style={styles.primaryButton}
              onPress={() => router.push('/(auth)/sign-in' as any)}
              activeOpacity={0.8}
            >
              <Text style={styles.primaryButtonText}>Continue to Sign In</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.secondaryButton}
              onPress={() => router.push('/(auth)/sign-up' as any)}
              activeOpacity={0.8}
            >
              <Text style={styles.secondaryButtonText}>Create an Account</Text>
            </TouchableOpacity>

            <View style={styles.footerNote}>
              <Text style={styles.footerNoteText}>
                Protected by Clerk Authentication & Local Device Biometrics
              </Text>
            </View>
          </View>
        </SignedOut>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#070A0F',
  },
  scrollContent: {
    padding: 24,
    justifyContent: 'center',
    minHeight: '100%',
  },
  brandHeader: {
    alignItems: 'center',
    marginBottom: 32,
  },
  logoBadge: {
    width: 64,
    height: 64,
    borderRadius: 20,
    backgroundColor: '#00D084',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
    shadowColor: '#00D084',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.35,
    shadowRadius: 16,
    elevation: 8,
  },
  logoText: {
    fontSize: 32,
    fontWeight: '800',
    color: '#070A0F',
  },
  appName: {
    fontSize: 32,
    fontWeight: '800',
    color: '#FFFFFF',
    letterSpacing: -0.5,
  },
  appTagline: {
    fontSize: 14,
    color: '#94A3B8',
    marginTop: 6,
    textAlign: 'center',
  },
  card: {
    backgroundColor: '#0F1626',
    borderRadius: 24,
    padding: 24,
    borderWidth: 1,
    borderColor: '#1E293B',
  },
  cardTitle: {
    fontSize: 22,
    fontWeight: '700',
    color: '#FFFFFF',
    marginBottom: 6,
  },
  cardSubtitle: {
    fontSize: 14,
    color: '#94A3B8',
    marginBottom: 24,
    lineHeight: 20,
  },
  primaryButton: {
    backgroundColor: '#00D084',
    borderRadius: 14,
    paddingVertical: 16,
    alignItems: 'center',
    marginBottom: 12,
  },
  primaryButtonText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#070A0F',
  },
  secondaryButton: {
    backgroundColor: '#1E293B',
    borderRadius: 14,
    paddingVertical: 16,
    alignItems: 'center',
    marginBottom: 16,
  },
  secondaryButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#F8FAFC',
  },
  footerNote: {
    alignItems: 'center',
    marginTop: 8,
  },
  footerNoteText: {
    fontSize: 12,
    color: '#64748B',
    textAlign: 'center',
  },
  userHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#00D084',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 14,
  },
  avatarText: {
    fontSize: 20,
    fontWeight: '700',
    color: '#070A0F',
  },
  userInfo: {
    flex: 1,
  },
  welcomeText: {
    fontSize: 13,
    color: '#94A3B8',
  },
  userName: {
    fontSize: 18,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  divider: {
    height: 1,
    backgroundColor: '#1E293B',
    marginVertical: 16,
  },
  environmentGrid: {
    gap: 12,
    marginBottom: 20,
  },
  envButton: {
    borderRadius: 16,
    padding: 18,
    borderWidth: 1,
  },
  moneyEnvButton: {
    backgroundColor: '#0B1A14',
    borderColor: '#059669',
  },
  investEnvButton: {
    backgroundColor: '#1A160A',
    borderColor: '#D97706',
  },
  envButtonBadge: {
    alignSelf: 'flex-start',
    fontSize: 11,
    fontWeight: '700',
    textTransform: 'uppercase',
    color: '#00D084',
    marginBottom: 6,
  },
  investBadge: {
    color: '#F59E0B',
  },
  envButtonTitle: {
    fontSize: 17,
    fontWeight: '700',
    color: '#FFFFFF',
    marginBottom: 4,
  },
  envButtonDesc: {
    fontSize: 13,
    color: '#94A3B8',
  },
  signOutButton: {
    paddingVertical: 14,
    alignItems: 'center',
    borderRadius: 12,
    backgroundColor: '#1E293B',
  },
  signOutText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#EF4444',
  },
});
