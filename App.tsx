import React, { useState } from 'react';
import { SafeAreaView, StatusBar, StyleSheet, Text, TouchableOpacity, View, ScrollView } from 'react-native';
import CustomerDashboard from './src/screens/CustomerDashboard';
import ManagerDashboard from './src/screens/ManagerDashboard';
import DriverDashboard from './src/screens/DriverDashboard';
import { COLORS, SHADOWS } from './src/theme/colors';

export { COLORS };

function App(): React.JSX.Element {
  const [currentRole, setCurrentRole] = useState<'NONE' | 'CUSTOMER' | 'MANAGER' | 'DRIVER'>('NONE');

  const renderDashboard = () => {
    switch (currentRole) {
      case 'CUSTOMER':
        return <CustomerDashboard onLogout={() => setCurrentRole('NONE')} />;
      case 'MANAGER':
        return <ManagerDashboard onLogout={() => setCurrentRole('NONE')} />;
      case 'DRIVER':
        return <DriverDashboard onLogout={() => setCurrentRole('NONE')} />;
      default:
        return (
          <ScrollView contentContainerStyle={styles.landingContainer}>
            {/* Top Branding Section */}
            <View style={styles.brandHero}>
              <View style={[styles.logoIconBadge, SHADOWS.glowRed]}>
                <Text style={styles.logoEmoji}>🚨</Text>
              </View>
              <Text style={styles.appTitle}>WRECK4LESS</Text>
              <Text style={styles.appTagline}>PRIVATE DISPATCH & FLEET SYSTEM</Text>
              
              <View style={styles.statusPill}>
                <View style={styles.statusDot} />
                <Text style={styles.statusPillText}>DISPATCH ENGINE ONLINE</Text>
              </View>
            </View>

            {/* Portal Selection Cards */}
            <Text style={styles.sectionHeader}>SELECT ACCESS PORTAL</Text>

            <TouchableOpacity
              activeOpacity={0.85}
              style={[styles.portalCard, styles.customerCardHighlight]}
              onPress={() => setCurrentRole('CUSTOMER')}
            >
              <View style={[styles.portalIconBox, { backgroundColor: 'rgba(239, 68, 68, 0.15)' }]}>
                <Text style={styles.portalIcon}>🚗</Text>
              </View>
              <View style={styles.portalInfo}>
                <View style={styles.portalHeaderRow}>
                  <Text style={styles.portalTitle}>CUSTOMER PORTAL</Text>
                  <Text style={styles.portalTag}>DIRECT ASSIST</Text>
                </View>
                <Text style={styles.portalDescription}>
                  Request emergency dispatch, view live tow tracking & fast checkout
                </Text>
              </View>
              <Text style={styles.portalChevron}>›</Text>
            </TouchableOpacity>

            <TouchableOpacity
              activeOpacity={0.85}
              style={styles.portalCard}
              onPress={() => setCurrentRole('DRIVER')}
            >
              <View style={[styles.portalIconBox, { backgroundColor: 'rgba(59, 130, 246, 0.15)' }]}>
                <Text style={styles.portalIcon}>🚚</Text>
              </View>
              <View style={styles.portalInfo}>
                <View style={styles.portalHeaderRow}>
                  <Text style={styles.portalTitle}>DRIVER TERMINAL</Text>
                  <Text style={[styles.portalTag, { color: '#60A5FA', borderColor: 'rgba(59, 130, 246, 0.4)' }]}>OPERATIONS</Text>
                </View>
                <Text style={styles.portalDescription}>
                  Shift toggles, turn-by-turn routing, job acceptance & on-site logs
                </Text>
              </View>
              <Text style={styles.portalChevron}>›</Text>
            </TouchableOpacity>

            <TouchableOpacity
              activeOpacity={0.85}
              style={styles.portalCard}
              onPress={() => setCurrentRole('MANAGER')}
            >
              <View style={[styles.portalIconBox, { backgroundColor: 'rgba(245, 158, 11, 0.15)' }]}>
                <Text style={styles.portalIcon}>🛡️</Text>
              </View>
              <View style={styles.portalInfo}>
                <View style={styles.portalHeaderRow}>
                  <Text style={styles.portalTitle}>FLEET MANAGER</Text>
                  <Text style={[styles.portalTag, { color: '#FBBF24', borderColor: 'rgba(245, 158, 11, 0.4)' }]}>COMMAND</Text>
                </View>
                <Text style={styles.portalDescription}>
                  Full dispatch queue oversight, unit reassignments & analytics
                </Text>
              </View>
              <Text style={styles.portalChevron}>›</Text>
            </TouchableOpacity>

            {/* Footer telematics badge */}
            <View style={styles.footerInfo}>
              <Text style={styles.footerText}>FASTAPI BACKEND • POSTGRESQL ENGINE</Text>
              <Text style={styles.footerSub}>v2.4.0 • HIGH-SATURATION FLEET READY</Text>
            </View>
          </ScrollView>
        );
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor={COLORS.background} />
      {renderDashboard()}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.background,
  },
  landingContainer: {
    paddingHorizontal: 20,
    paddingTop: 36,
    paddingBottom: 24,
    justifyContent: 'center',
  },
  brandHero: {
    alignItems: 'center',
    marginBottom: 36,
  },
  logoIconBadge: {
    width: 68,
    height: 68,
    borderRadius: 20,
    backgroundColor: '#1C1315',
    borderWidth: 1.5,
    borderColor: COLORS.brandRedBorder,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },
  logoEmoji: {
    fontSize: 32,
  },
  appTitle: {
    fontSize: 32,
    fontWeight: '900',
    color: COLORS.textPrimary,
    letterSpacing: 2,
    fontStyle: 'italic',
  },
  appTagline: {
    fontSize: 11,
    fontWeight: '800',
    color: COLORS.brandRed,
    letterSpacing: 1.5,
    marginTop: 4,
  },
  statusPill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(16, 185, 129, 0.1)',
    borderColor: 'rgba(16, 185, 129, 0.3)',
    borderWidth: 1,
    borderRadius: 20,
    paddingHorizontal: 12,
    paddingVertical: 4,
    marginTop: 14,
  },
  statusDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: COLORS.statusGreen,
    marginRight: 6,
  },
  statusPillText: {
    color: COLORS.statusGreen,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  sectionHeader: {
    fontSize: 12,
    fontWeight: '800',
    color: COLORS.textMuted,
    letterSpacing: 1,
    marginBottom: 14,
    marginLeft: 4,
  },
  portalCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: COLORS.cardBg,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: COLORS.borderSubtle,
    padding: 16,
    marginBottom: 14,
  },
  customerCardHighlight: {
    borderColor: COLORS.brandRedBorder,
    backgroundColor: '#141216',
  },
  portalIconBox: {
    width: 48,
    height: 48,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 14,
  },
  portalIcon: {
    fontSize: 22,
  },
  portalInfo: {
    flex: 1,
  },
  portalHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  portalTitle: {
    color: COLORS.textPrimary,
    fontSize: 15,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  portalTag: {
    fontSize: 9,
    fontWeight: '900',
    color: COLORS.brandRedLight,
    borderWidth: 1,
    borderColor: COLORS.brandRedBorder,
    paddingHorizontal: 6,
    paddingVertical: 1,
    borderRadius: 6,
    marginLeft: 8,
  },
  portalDescription: {
    color: COLORS.textSecondary,
    fontSize: 12,
    lineHeight: 16,
  },
  portalChevron: {
    color: COLORS.textMuted,
    fontSize: 22,
    fontWeight: 'bold',
    marginLeft: 8,
  },
  footerInfo: {
    alignItems: 'center',
    marginTop: 28,
  },
  footerText: {
    color: COLORS.textMuted,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1,
  },
  footerSub: {
    color: 'rgba(255, 255, 255, 0.25)',
    fontSize: 9,
    fontWeight: '700',
    marginTop: 3,
  },
});

export default App;
