import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { COLORS } from '../../App';
import MapView, { UrlTile, Marker } from 'react-native-maps';

export default function DriverDashboard({ onLogout }: { onLogout: () => void }) {
  const [isOnline, setIsOnline] = useState(true);
  const [jobStatus, setJobStatus] = useState<'EN_ROUTE' | 'ON_SITE' | 'COMPLETED'>('EN_ROUTE');

  return (
    <View style={styles.container}>
      <View style={styles.topBar}>
        <Text style={styles.header}>DRIVER MENU</Text>
        <TouchableOpacity 
          style={[styles.statusToggle, !isOnline && { backgroundColor: COLORS.cardGray }]} 
          onPress={() => setIsOnline(!isOnline)}
        >
          <Text style={{ color: isOnline ? COLORS.pureBlack : COLORS.gray, fontWeight: 'bold' }}>
            {isOnline ? 'ONLINE' : 'OFFLINE'}
          </Text>
        </TouchableOpacity>
      </View>

      {!isOnline ? (
        <View style={styles.centerBox}>
          <Text style={{ color: COLORS.brandRed, fontSize: 24, fontWeight: '900' }}>YOU ARE OFFLINE</Text>
          <Text style={{ color: COLORS.gray }}>Go online to receive dispatches.</Text>
        </View>
      ) : jobStatus === 'COMPLETED' ? (
        <View style={styles.centerBox}>
          <Text style={{ color: COLORS.brandRed, fontSize: 24, fontWeight: '900' }}>STANDBY</Text>
          <Text style={{ color: COLORS.gray }}>Waiting for next dispatch...</Text>
        </View>
      ) : (
        <View style={{ flex: 1 }}>
          <View style={{ padding: 24, paddingBottom: 12 }}>
            <Text style={{ color: COLORS.brandRed, fontWeight: 'bold' }}>ACTIVE DISPATCH</Text>
            <Text style={{ color: COLORS.white, fontSize: 18, fontWeight: 'bold' }}>JOB-9481 • Toyota Camry</Text>
            <Text style={{ color: COLORS.gray }}>📍 14th St & Broadway</Text>
          </View>
          
          <MapView
            style={{ flex: 1, marginHorizontal: 24, borderRadius: 16, overflow: 'hidden' }}
            mapType="none"
            initialRegion={{
              latitude: 40.7128,
              longitude: -74.0060,
              latitudeDelta: 0.05,
              longitudeDelta: 0.05,
            }}
          >
            <UrlTile urlTemplate="https://a.tile.openstreetmap.org/{z}/{x}/{y}.png" maximumZ={19} />
            <Marker coordinate={{ latitude: 40.7100, longitude: -74.0010 }} title="You" description="Unit 4" pinColor="red" />
            <Marker coordinate={{ latitude: 40.7128, longitude: -74.0060 }} title="Pickup" description="Toyota Camry" />
          </MapView>

          <View style={{ padding: 24 }}>
            <TouchableOpacity 
              style={styles.primaryBtn} 
              onPress={() => setJobStatus(jobStatus === 'EN_ROUTE' ? 'ON_SITE' : 'COMPLETED')}
            >
              <Text style={styles.primaryBtnText}>
                {jobStatus === 'EN_ROUTE' ? 'MARK ON SITE' : 'MARK JOB COMPLETED'}
              </Text>
            </TouchableOpacity>
          </View>
        </View>
      )}

      <TouchableOpacity style={styles.logoutBtn} onPress={onLogout}>
        <Text style={styles.logoutText}>SIGN OUT</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.pureBlack, paddingTop: 60 },
  topBar: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 24, marginBottom: 12 },
  header: { fontSize: 24, fontWeight: '900', color: '#FFFFFF' },
  statusToggle: { backgroundColor: COLORS.brandRed, paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20 },
  centerBox: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  primaryBtn: { backgroundColor: COLORS.brandRed, padding: 20, borderRadius: 16, alignItems: 'center' },
  primaryBtnText: { color: COLORS.pureBlack, fontWeight: '900' },
  logoutBtn: { alignSelf: 'center', padding: 24 },
  logoutText: { color: COLORS.gray, fontWeight: '900' },
});
