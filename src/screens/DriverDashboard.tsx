import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Alert } from 'react-native';
import { COLORS, SHADOWS } from '../theme/colors';
import Header from '../components/Header';
import Button from '../components/Button';
import Card from '../components/Card';
import StatusBadge from '../components/StatusBadge';
import MapView, { UrlTile, Marker } from 'react-native-maps';

interface DriverDashboardProps {
  onLogout: () => void;
}

export default function DriverDashboard({ onLogout }: DriverDashboardProps) {
  const [isOnline, setIsOnline] = useState(true);
  const [jobStatus, setJobStatus] = useState<'EN_ROUTE' | 'ON_SITE' | 'COMPLETED'>('EN_ROUTE');

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      <Header
        title="Driver Terminal"
        subtitle="Unit #4 • Flatbed Carrier"
        roleBadge="DRIVER"
        onLogout={onLogout}
      />

      <View style={styles.body}>
        {/* Shift Availability Toggle Card */}
        <Card variant="raised" style={styles.shiftCard}>
          <View style={styles.shiftRow}>
            <View>
              <Text style={styles.shiftTitle}>CURRENT SHIFT STATUS</Text>
              <View style={{ marginTop: 4 }}>
                <StatusBadge status={isOnline ? 'ONLINE' : 'OFFLINE'} />
              </View>
            </View>

            <TouchableOpacity
              activeOpacity={0.8}
              style={[styles.toggleBtn, isOnline ? styles.toggleOnline : styles.toggleOffline]}
              onPress={() => setIsOnline(!isOnline)}
            >
              <View style={[styles.toggleCircle, isOnline ? styles.circleOnline : styles.circleOffline]} />
              <Text style={[styles.toggleText, { color: isOnline ? '#09090B' : COLORS.textMuted }]}>
                {isOnline ? 'ONLINE' : 'OFFLINE'}
              </Text>
            </TouchableOpacity>
          </View>
        </Card>

        {/* Offline State */}
        {!isOnline ? (
          <Card variant="default" style={styles.centerStandbyCard}>
            <Text style={styles.standbyEmoji}>⏸️</Text>
            <Text style={styles.standbyTitle}>UNIT CURRENTLY OFFLINE</Text>
            <Text style={styles.standbySub}>
              Toggle your status to Online to appear on dispatch radars and receive tow requests.
            </Text>
            <Button
              title="GO ONLINE NOW"
              onPress={() => setIsOnline(true)}
              style={{ marginTop: 18, width: '100%' }}
            />
          </Card>
        ) : jobStatus === 'COMPLETED' ? (
          /* Standby Queue Searching */
          <Card variant="default" style={styles.centerStandbyCard}>
            <View style={[styles.radarCircle, SHADOWS.glowRed]}>
              <Text style={styles.radarEmoji}>📡</Text>
            </View>
            <Text style={styles.standbyTitle}>SCANNING FOR DISPATCHES</Text>
            <Text style={styles.standbySub}>
              Unit #4 is stationed at top priority for Manhattan South Sector. Keep your phone nearby.
            </Text>
            <Button
              title="SIMULATE NEW DISPATCH CALL"
              variant="secondary"
              onPress={() => setJobStatus('EN_ROUTE')}
              style={{ marginTop: 20, width: '100%' }}
            />
          </Card>
        ) : (
          /* Active Job In Progress */
          <View>
            {/* Step Progress Indicator */}
            <View style={styles.stepperContainer}>
              <View style={styles.stepItem}>
                <View style={[styles.stepDot, styles.stepDotActive]}>
                  <Text style={styles.stepNum}>1</Text>
                </View>
                <Text style={styles.stepLabelActive}>En Route</Text>
              </View>
              <View style={[styles.stepLine, jobStatus === 'ON_SITE' && styles.stepLineActive]} />
              <View style={styles.stepItem}>
                <View style={[styles.stepDot, jobStatus === 'ON_SITE' ? styles.stepDotActive : styles.stepDotInactive]}>
                  <Text style={styles.stepNum}>2</Text>
                </View>
                <Text style={jobStatus === 'ON_SITE' ? styles.stepLabelActive : styles.stepLabelInactive}>
                  On Site
                </Text>
              </View>
              <View style={styles.stepLine} />
              <View style={styles.stepItem}>
                <View style={[styles.stepDot, styles.stepDotInactive]}>
                  <Text style={styles.stepNum}>3</Text>
                </View>
                <Text style={styles.stepLabelInactive}>Clear</Text>
              </View>
            </View>

            {/* Job Details Card */}
            <Card variant="raised" style={{ marginBottom: 16 }}>
              <View style={styles.jobHeaderRow}>
                <View>
                  <Text style={styles.jobIdTag}>DISPATCH #WRK-9481</Text>
                  <Text style={styles.jobVehicle}>2021 Toyota Camry (Overheated)</Text>
                </View>
                <Text style={styles.jobRateText}>$145.00</Text>
              </View>

              <View style={styles.locationBlock}>
                <View style={styles.locationRow}>
                  <Text style={styles.locIcon}>📍</Text>
                  <View style={{ flex: 1 }}>
                    <Text style={styles.locSub}>PICKUP LOCATION</Text>
                    <Text style={styles.locMain}>14th St & Broadway, New York, NY</Text>
                  </View>
                </View>

                <View style={styles.locationRow}>
                  <Text style={styles.locIcon}>🏁</Text>
                  <View style={{ flex: 1 }}>
                    <Text style={styles.locSub}>DROP-OFF DESTINATION</Text>
                    <Text style={styles.locMain}>Empire Auto Storage Yard (Brooklyn, NY)</Text>
                  </View>
                </View>
              </View>

              <View style={styles.actionRow}>
                <TouchableOpacity
                  style={styles.quickActionBtn}
                  onPress={() => Alert.alert('Customer Phone', 'Dialing customer: (555) 392-1084')}
                >
                  <Text style={styles.quickActionText}>📞 CALL CUSTOMER</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={styles.quickActionBtn}
                  onPress={() => Alert.alert('Navigation', 'Launching external GPS turn-by-turn...')}
                >
                  <Text style={styles.quickActionText}>🧭 OPEN GPS</Text>
                </TouchableOpacity>
              </View>
            </Card>

            {/* Map Preview */}
            <View style={styles.mapContainer}>
              <MapView
                style={StyleSheet.absoluteFill}
                mapType="none"
                initialRegion={{
                  latitude: 40.7128,
                  longitude: -74.006,
                  latitudeDelta: 0.04,
                  longitudeDelta: 0.04,
                }}
              >
                <UrlTile urlTemplate="https://a.tile.openstreetmap.org/{z}/{x}/{y}.png" maximumZ={19} />
                <Marker
                  coordinate={{ latitude: 40.7105, longitude: -74.0018 }}
                  title="Your Unit (#4)"
                  pinColor="red"
                />
                <Marker
                  coordinate={{ latitude: 40.7128, longitude: -74.006 }}
                  title="Pickup Vehicle"
                  description="Toyota Camry"
                />
              </MapView>
            </View>

            {/* Step Progression Button */}
            <Button
              title={jobStatus === 'EN_ROUTE' ? 'MARK ON SITE & HOOKING' : 'COMPLETE TOW & RELEASE UNIT'}
              onPress={() => {
                if (jobStatus === 'EN_ROUTE') {
                  setJobStatus('ON_SITE');
                } else {
                  setJobStatus('COMPLETED');
                  Alert.alert('Job Completed', 'Tow completed and logged. Rate $145.00 credited.');
                }
              }}
              icon="✅"
              style={{ marginTop: 16 }}
            />
          </View>
        )}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.background,
  },
  contentContainer: {
    paddingBottom: 36,
  },
  body: {
    paddingHorizontal: 20,
    paddingTop: 16,
  },
  shiftCard: {
    marginBottom: 20,
  },
  shiftRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  shiftTitle: {
    color: COLORS.textMuted,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1,
  },
  toggleBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 24,
    borderWidth: 1,
  },
  toggleOnline: {
    backgroundColor: COLORS.statusGreen,
    borderColor: COLORS.statusGreen,
  },
  toggleOffline: {
    backgroundColor: COLORS.cardBg,
    borderColor: COLORS.borderActive,
  },
  toggleCircle: {
    width: 12,
    height: 12,
    borderRadius: 6,
    marginRight: 6,
  },
  circleOnline: {
    backgroundColor: '#09090B',
  },
  circleOffline: {
    backgroundColor: COLORS.textMuted,
  },
  toggleText: {
    fontSize: 11,
    fontWeight: '900',
    letterSpacing: 0.5,
  },
  centerStandbyCard: {
    alignItems: 'center',
    padding: 32,
    marginVertical: 20,
  },
  standbyEmoji: {
    fontSize: 48,
    marginBottom: 16,
  },
  radarCircle: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: '#1E1315',
    borderWidth: 1.5,
    borderColor: COLORS.brandRedBorder,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },
  radarEmoji: {
    fontSize: 32,
  },
  standbyTitle: {
    color: COLORS.textPrimary,
    fontSize: 18,
    fontWeight: '900',
    letterSpacing: 0.5,
    textAlign: 'center',
  },
  standbySub: {
    color: COLORS.textSecondary,
    fontSize: 12,
    textAlign: 'center',
    lineHeight: 18,
    marginTop: 8,
  },
  stepperContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 16,
    paddingHorizontal: 10,
  },
  stepItem: {
    alignItems: 'center',
  },
  stepDot: {
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 4,
  },
  stepDotActive: {
    backgroundColor: COLORS.brandRed,
  },
  stepDotInactive: {
    backgroundColor: COLORS.cardRaised,
    borderWidth: 1,
    borderColor: COLORS.borderSubtle,
  },
  stepNum: {
    color: '#09090B',
    fontWeight: '900',
    fontSize: 12,
  },
  stepLabelActive: {
    color: COLORS.brandRedLight,
    fontWeight: '800',
    fontSize: 10,
  },
  stepLabelInactive: {
    color: COLORS.textMuted,
    fontSize: 10,
  },
  stepLine: {
    flex: 1,
    height: 2,
    backgroundColor: COLORS.cardRaised,
    marginHorizontal: 8,
    marginBottom: 16,
  },
  stepLineActive: {
    backgroundColor: COLORS.brandRed,
  },
  jobHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 14,
  },
  jobIdTag: {
    color: COLORS.brandRed,
    fontWeight: '900',
    fontSize: 11,
    letterSpacing: 0.8,
  },
  jobVehicle: {
    color: COLORS.textPrimary,
    fontWeight: '800',
    fontSize: 16,
    marginTop: 2,
  },
  jobRateText: {
    color: COLORS.statusGreen,
    fontWeight: '900',
    fontSize: 18,
  },
  locationBlock: {
    backgroundColor: COLORS.inputBg,
    borderRadius: 12,
    padding: 12,
    marginBottom: 14,
  },
  locationRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 4,
  },
  locIcon: {
    fontSize: 16,
    marginRight: 10,
  },
  locSub: {
    color: COLORS.textMuted,
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  locMain: {
    color: COLORS.textPrimary,
    fontSize: 13,
    fontWeight: '600',
  },
  actionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  quickActionBtn: {
    flex: 0.48,
    backgroundColor: COLORS.cardRaised,
    borderWidth: 1,
    borderColor: COLORS.borderActive,
    borderRadius: 10,
    paddingVertical: 10,
    alignItems: 'center',
  },
  quickActionText: {
    color: COLORS.textPrimary,
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  mapContainer: {
    height: 180,
    borderRadius: 16,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: COLORS.borderSubtle,
    marginBottom: 8,
  },
});
