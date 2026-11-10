import { useRouter } from 'expo-router';
import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, SafeAreaView } from 'react-native';

export default function MoneyScreen() {
  const router = useRouter();

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <View style={styles.badge}>
          <Text style={styles.badgeText}>Money Management</Text>
        </View>
        <Text style={styles.title}>Daily Cash & Budgets</Text>
        <Text style={styles.subtitle}>
          Track Cash, M-PESA, Category Budgets, Bills, and Debts.
        </Text>

        <TouchableOpacity
          style={styles.switchButton}
          onPress={() => router.push('/(tabs)/investments' as any)}
          activeOpacity={0.8}
        >
          <Text style={styles.switchButtonText}>Switch to Investments →</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.backButton}
          onPress={() => router.replace('/' as any)}
          activeOpacity={0.8}
        >
          <Text style={styles.backButtonText}>← Home</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#070A0F',
  },
  content: {
    flex: 1,
    padding: 24,
    justifyContent: 'center',
    alignItems: 'center',
  },
  badge: {
    backgroundColor: '#0B1A14',
    borderColor: '#059669',
    borderWidth: 1,
    borderRadius: 20,
    paddingHorizontal: 14,
    paddingVertical: 6,
    marginBottom: 16,
  },
  badgeText: {
    color: '#00D084',
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  title: {
    fontSize: 26,
    fontWeight: '800',
    color: '#FFFFFF',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#94A3B8',
    textAlign: 'center',
    marginBottom: 32,
    lineHeight: 20,
  },
  switchButton: {
    backgroundColor: '#1E293B',
    borderRadius: 14,
    paddingVertical: 14,
    paddingHorizontal: 24,
    marginBottom: 12,
  },
  switchButtonText: {
    color: '#F59E0B',
    fontSize: 15,
    fontWeight: '700',
  },
  backButton: {
    paddingVertical: 12,
  },
  backButtonText: {
    color: '#64748B',
    fontSize: 14,
  },
});
