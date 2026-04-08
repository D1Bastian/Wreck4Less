import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, TextInput } from 'react-native';
import { COLORS } from '../../App';
import MapView, { UrlTile, Marker } from 'react-native-maps';

export default function CustomerDashboard({ onLogout }: { onLogout: () => void }) {
  const [view, setView] = useState<'home' | 'form' | 'payment' | 'tracking'>('home');

  const renderHome = () => (
    <View style={styles.container}>
      <Text style={styles.header}>SUPPORT</Text>
      
      <TouchableOpacity style={styles.bigRedCard} onPress={() => setView('form')}>
        <Text style={styles.bigCardTitle}>REQUEST A{"\n"}WRECK</Text>
        <Text style={styles.bigCardSub}>Tap for private dispatch</Text>
      </TouchableOpacity>

      <View style={styles.row}>
        <TouchableOpacity style={styles.actionCard}>
          <Text style={styles.actionIcon}>⛽</Text>
          <Text style={styles.actionText}>Gas Stations</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.actionCard}>
          <Text style={styles.actionIcon}>🛡️</Text>
          <Text style={styles.actionText}>Emergency</Text>
        </TouchableOpacity>
      </View>
      
      <View style={{ flex: 1 }} />
      <TouchableOpacity style={styles.logoutBtn} onPress={onLogout}>
        <Text style={styles.logoutText}>SIGN OUT</Text>
      </TouchableOpacity>
    </View>
  );

  const renderForm = () => (
    <ScrollView style={styles.container}>
      <Text style={styles.header}>Wrecking Details</Text>
      <TextInput placeholder="Make (e.g. Toyota)" placeholderTextColor={COLORS.gray} style={styles.input} />
      <TextInput placeholder="Model (e.g. Camry)" placeholderTextColor={COLORS.gray} style={styles.input} />
      <TextInput placeholder="Year" placeholderTextColor={COLORS.gray} style={styles.input} />
      <TextInput placeholder="Damage Description" placeholderTextColor={COLORS.gray} style={[styles.input, { height: 100 }]} multiline />
      <TextInput placeholder="Contact Phone" placeholderTextColor={COLORS.gray} style={styles.input} />
      
      <TouchableOpacity style={styles.primaryBtn} onPress={() => setView('payment')}>
        <Text style={styles.primaryBtnText}>SUBMIT FOR DISPATCH</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.logoutBtn} onPress={() => setView('home')}>
        <Text style={styles.logoutText}>CANCEL</Text>
      </TouchableOpacity>
    </ScrollView>
  );

  const renderPayment = () => (
    <View style={styles.container}>
      <Text style={styles.header}>Payment Choice</Text>
      <Text style={{ color: COLORS.white, marginBottom: 24 }}>Your approved rate: $195.00</Text>
      
      <TouchableOpacity style={styles.actionCardFull} onPress={() => setView('tracking')}>
        <Text style={styles.actionIcon}>💵</Text>
        <Text style={styles.actionText}>Pay Cash On Site</Text>
      </TouchableOpacity>
      <View style={{height: 12}} />
      <TouchableOpacity style={styles.actionCardFull} onPress={() => setView('tracking')}>
        <Text style={styles.actionIcon}>💳</Text>
        <Text style={styles.actionText}>Pay with Card</Text>
      </TouchableOpacity>
    </View>
  );

  const renderTracking = () => (
    <View style={{ flex: 1, backgroundColor: COLORS.pureBlack }}>
      <View style={{ padding: 24, paddingTop: 40 }}>
         <Text style={styles.header}>ACTIVE DISPATCH</Text>
         <Text style={{ color: COLORS.brandRed, fontWeight: 'bold' }}>DRIVER EN ROUTE</Text>
      </View>
      <MapView
        style={{ flex: 1 }}
        mapType="none"
        initialRegion={{
          latitude: 40.7128,
          longitude: -74.0060,
          latitudeDelta: 0.05,
          longitudeDelta: 0.05,
        }}
      >
        <UrlTile urlTemplate="https://a.tile.openstreetmap.org/{z}/{x}/{y}.png" maximumZ={19} />
        <Marker coordinate={{ latitude: 40.7128, longitude: -74.0060 }} title="You" description="Your vehicle" />
        <Marker coordinate={{ latitude: 40.7100, longitude: -74.0010 }} title="Tow Truck" description="En route" pinColor="red" />
      </MapView>

      <View style={{ padding: 24, paddingBottom: 40 }}>
         <TouchableOpacity style={styles.logoutBtn} onPress={() => setView('home')}>
             <Text style={styles.logoutText}>RETURN HOME</Text>
         </TouchableOpacity>
      </View>
    </View>
  );

  switch (view) {
    case 'form': return renderForm();
    case 'payment': return renderPayment();
    case 'tracking': return renderTracking();
    default: return renderHome();
  }
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, backgroundColor: COLORS.pureBlack },
  header: { fontSize: 24, fontWeight: '900', color: '#FFFFFF', marginBottom: 20, marginTop: 20 },
  bigRedCard: { backgroundColor: COLORS.brandRed, borderRadius: 32, height: 220, padding: 32, justifyContent: 'center', marginBottom: 24 },
  bigCardTitle: { fontSize: 40, fontWeight: '900', color: COLORS.pureBlack, lineHeight: 42 },
  bigCardSub: { fontSize: 14, fontWeight: 'bold', color: 'rgba(0,0,0,0.6)', marginTop: 8 },
  row: { flexDirection: 'row', justifyContent: 'space-between' },
  actionCard: { backgroundColor: COLORS.cardGray, borderRadius: 24, height: 140, flex: 0.48, alignItems: 'center', justifyContent: 'center' },
  actionCardFull: { backgroundColor: COLORS.cardGray, borderRadius: 24, paddingVertical: 24, alignItems: 'center', justifyContent: 'center' },
  actionIcon: { fontSize: 32, marginBottom: 12 },
  actionText: { color: COLORS.white, fontSize: 14, fontWeight: '900' },
  input: { backgroundColor: COLORS.cardGray, color: COLORS.white, padding: 16, borderRadius: 12, marginBottom: 12 },
  primaryBtn: { backgroundColor: COLORS.brandRed, padding: 20, borderRadius: 16, alignItems: 'center', marginTop: 24 },
  primaryBtnText: { color: COLORS.pureBlack, fontWeight: '900' },
  logoutBtn: { alignSelf: 'center', padding: 12, marginTop: 12 },
  logoutText: { color: COLORS.gray, fontWeight: '900' },
});
