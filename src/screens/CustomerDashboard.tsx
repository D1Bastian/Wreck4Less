import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Alert } from 'react-native';
import { COLORS, SHADOWS } from '../theme/colors';
import Header from '../components/Header';
import Button from '../components/Button';
import Input from '../components/Input';
import Card from '../components/Card';
import StatusBadge from '../components/StatusBadge';
import MapView, { UrlTile, Marker } from 'react-native-maps';

interface CustomerDashboardProps {
  onLogout: () => void;
}

export default function CustomerDashboard({ onLogout }: CustomerDashboardProps) {
  const [view, setView] = useState<'home' | 'form' | 'payment' | 'tracking'>('home');
  const [vehicleMake, setVehicleMake] = useState('Toyota');
  const [vehicleModel, setVehicleModel] = useState('Camry');
  const [vehicleYear, setVehicleYear] = useState('2021');
  const [damageDesc, setDamageDesc] = useState('Front engine stall after highway overheating');
  const [phone, setPhone] = useState('(555) 392-1084');
  const [pickupLocation, setPickupLocation] = useState('14th St & Broadway, NY');
  const [selectedService, setSelectedService] = useState('TOW');
  const [selectedPayment, setSelectedPayment] = useState<'CARD' | 'CASH' | 'DIGITAL'>('CARD');

  const renderHome = () => (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      <Header
        title="Emergency Assist"
        subtitle="Live Towing & Roadside Network"
        roleBadge="CUSTOMER"
        onLogout={onLogout}
      />

      <View style={styles.body}>
        {/* Hero Emergency Request Card */}
        <TouchableOpacity
          activeOpacity={0.88}
          style={[styles.heroCard, SHADOWS.glowRed]}
          onPress={() => setView('form')}
        >
          <View style={styles.heroTopRow}>
            <View style={styles.heroTag}>
              <Text style={styles.heroTagText}>🚨 PRIORITY DISPATCH</Text>
            </View>
            <Text style={styles.heroEta}>⏱️ ~15 MIN ETA</Text>
          </View>

          <Text style={styles.heroTitle}>REQUEST A WRECK</Text>
          <Text style={styles.heroSubtitle}>
            Instant roadside dispatch. Certified flatbeds, wheel-lifts & lockout specialists en route.
          </Text>

          <View style={styles.heroActionRow}>
            <Text style={styles.heroActionText}>Tap to Start Request</Text>
            <View style={styles.heroActionArrow}>
              <Text style={styles.arrowText}>→</Text>
            </View>
          </View>
        </TouchableOpacity>

        {/* Quick Roadside Assistance Grid */}
        <Text style={styles.sectionTitle}>ROADSIDE SERVICES</Text>
        <View style={styles.serviceGrid}>
          <TouchableOpacity
            style={[styles.serviceTile, selectedService === 'TOW' && styles.serviceTileActive]}
            onPress={() => setSelectedService('TOW')}
          >
            <Text style={styles.serviceIcon}>🚚</Text>
            <Text style={styles.serviceName}>Flatbed Tow</Text>
            <Text style={styles.serviceSub}>All Vehicles</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.serviceTile, selectedService === 'FUEL' && styles.serviceTileActive]}
            onPress={() => setSelectedService('FUEL')}
          >
            <Text style={styles.serviceIcon}>⛽</Text>
            <Text style={styles.serviceName}>Fuel Delivery</Text>
            <Text style={styles.serviceSub}>5 Gallons</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.serviceTile, selectedService === 'TIRE' && styles.serviceTileActive]}
            onPress={() => setSelectedService('TIRE')}
          >
            <Text style={styles.serviceIcon}>🛞</Text>
            <Text style={styles.serviceName}>Flat Tire</Text>
            <Text style={styles.serviceSub}>Spare Install</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.serviceTile, selectedService === 'LOCK' && styles.serviceTileActive]}
            onPress={() => setSelectedService('LOCK')}
          >
            <Text style={styles.serviceIcon}>🔑</Text>
            <Text style={styles.serviceName}>Lockout</Text>
            <Text style={styles.serviceSub}>Fast Entry</Text>
          </TouchableOpacity>
        </View>

        {/* Support & Quick Contact */}
        <Card variant="raised" style={styles.supportCard}>
          <View style={styles.supportRow}>
            <View style={styles.supportIconBox}>
              <Text style={styles.supportIcon}>🛡️</Text>
            </View>
            <View style={styles.supportTextColumn}>
              <Text style={styles.supportTitle}>24/7 Dispatch Hotline</Text>
              <Text style={styles.supportSub}>Direct operator line for extreme emergencies</Text>
            </View>
            <TouchableOpacity
              style={styles.callButton}
              onPress={() => Alert.alert('Hotline', 'Connecting you to Wreck4Less dispatch operator...')}
            >
              <Text style={styles.callButtonText}>CALL</Text>
            </TouchableOpacity>
          </View>
        </Card>
      </View>
    </ScrollView>
  );

  const renderForm = () => (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      <Header
        title="Vehicle & Location"
        subtitle="Provide dispatch details for driver"
        roleBadge="DISPATCH FORM"
        onBack={() => setView('home')}
      />

      <View style={styles.body}>
        <Card variant="raised" style={{ marginBottom: 20 }}>
          <Text style={styles.cardHeader}>VEHICLE SPECIFICATIONS</Text>
          <View style={styles.formRow}>
            <View style={{ flex: 1, marginRight: 8 }}>
              <Input label="Year" value={vehicleYear} onChangeText={setVehicleYear} keyboardType="numeric" />
            </View>
            <View style={{ flex: 2 }}>
              <Input label="Make" value={vehicleMake} onChangeText={setVehicleMake} />
            </View>
          </View>
          <Input label="Model" value={vehicleModel} onChangeText={setVehicleModel} />
          <Input
            label="Damage / Problem Description"
            value={damageDesc}
            onChangeText={setDamageDesc}
            multiline
            placeholder="Engine died, accident collision, flat tire, etc."
          />
        </Card>

        <Card variant="raised" style={{ marginBottom: 24 }}>
          <Text style={styles.cardHeader}>LOCATION & CONTACT</Text>
          <Input
            label="Pickup Location"
            value={pickupLocation}
            onChangeText={setPickupLocation}
            icon="📍"
          />
          <Input
            label="Driver Contact Phone"
            value={phone}
            onChangeText={setPhone}
            keyboardType="phone-pad"
            icon="📞"
          />
        </Card>

        <Button
          title="PROCEED TO PAYMENT & DISPATCH"
          onPress={() => setView('payment')}
          icon="⚡"
          style={{ marginBottom: 12 }}
        />
        <Button
          title="CANCEL REQUEST"
          variant="outline"
          onPress={() => setView('home')}
        />
      </View>
    </ScrollView>
  );

  const renderPayment = () => (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      <Header
        title="Payment & Rate"
        subtitle="Transparent automated billing"
        roleBadge="CHECKOUT"
        onBack={() => setView('form')}
      />

      <View style={styles.body}>
        {/* Rate Summary Card */}
        <Card variant="highlight" style={{ marginBottom: 24 }}>
          <Text style={styles.rateCardHeader}>DISPATCH RATE ESTIMATE</Text>
          <View style={styles.rateBreakdownRow}>
            <Text style={styles.rateLabel}>Standard Hookup Base</Text>
            <Text style={styles.rateValue}>$95.00</Text>
          </View>
          <View style={styles.rateBreakdownRow}>
            <Text style={styles.rateLabel}>Mileage (Flat Rate 5 mi)</Text>
            <Text style={styles.rateValue}>$35.00</Text>
          </View>
          <View style={styles.rateBreakdownRow}>
            <Text style={styles.rateLabel}>Priority Roadside Fee</Text>
            <Text style={styles.rateValue}>$15.00</Text>
          </View>
          <View style={styles.divider} />
          <View style={styles.rateTotalRow}>
            <Text style={styles.totalLabel}>TOTAL CONFIRMED</Text>
            <Text style={styles.totalAmount}>$145.00</Text>
          </View>
        </Card>

        <Text style={styles.sectionTitle}>SELECT PAYMENT METHOD</Text>

        <TouchableOpacity
          style={[styles.paymentMethodCard, selectedPayment === 'CARD' && styles.paymentMethodActive]}
          onPress={() => setSelectedPayment('CARD')}
        >
          <Text style={styles.paymentIcon}>💳</Text>
          <View style={{ flex: 1 }}>
            <Text style={styles.paymentMethodTitle}>Credit / Debit Card</Text>
            <Text style={styles.paymentMethodSub}>Visa ending in •••• 4242</Text>
          </View>
          <View style={[styles.radioCircle, selectedPayment === 'CARD' && styles.radioActive]} />
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.paymentMethodCard, selectedPayment === 'DIGITAL' && styles.paymentMethodActive]}
          onPress={() => setSelectedPayment('DIGITAL')}
        >
          <Text style={styles.paymentIcon}>📱</Text>
          <View style={{ flex: 1 }}>
            <Text style={styles.paymentMethodTitle}>Digital Wallet</Text>
            <Text style={styles.paymentMethodSub}>Apple Pay / Google Pay</Text>
          </View>
          <View style={[styles.radioCircle, selectedPayment === 'DIGITAL' && styles.radioActive]} />
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.paymentMethodCard, selectedPayment === 'CASH' && styles.paymentMethodActive]}
          onPress={() => setSelectedPayment('CASH')}
        >
          <Text style={styles.paymentIcon}>💵</Text>
          <View style={{ flex: 1 }}>
            <Text style={styles.paymentMethodTitle}>Pay Driver On Site</Text>
            <Text style={styles.paymentMethodSub}>Cash upon tow vehicle delivery</Text>
          </View>
          <View style={[styles.radioCircle, selectedPayment === 'CASH' && styles.radioActive]} />
        </TouchableOpacity>

        <Button
          title="AUTHORIZE & LAUNCH DISPATCH"
          onPress={() => setView('tracking')}
          icon="🚀"
          style={{ marginTop: 12, marginBottom: 12 }}
        />
        <Button
          title="BACK TO DETAILS"
          variant="outline"
          onPress={() => setView('form')}
        />
      </View>
    </ScrollView>
  );

  const renderTracking = () => (
    <View style={styles.trackingContainer}>
      <Header
        title="Live Dispatch"
        subtitle="Unit 4 En Route"
        roleBadge="TRACKING"
        onLogout={onLogout}
      />

      {/* Map View */}
      <View style={styles.mapWrapper}>
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
            coordinate={{ latitude: 40.7128, longitude: -74.006 }}
            title="Your Location"
            description="Toyota Camry (2021)"
          />
          <Marker
            coordinate={{ latitude: 40.7105, longitude: -74.0018 }}
            title="Tow Truck (Unit 4)"
            description="En route - 8 mins away"
            pinColor="red"
          />
        </MapView>
      </View>

      {/* Floating Driver Info Card */}
      <View style={[styles.trackingBottomCard, SHADOWS.card]}>
        <View style={styles.trackingHeaderRow}>
          <StatusBadge status="EN_ROUTE" label="UNIT EN ROUTE" />
          <Text style={styles.trackingEtaBadge}>⏱️ 8 MINS AWAY</Text>
        </View>

        <View style={styles.driverProfileRow}>
          <View style={styles.driverAvatar}>
            <Text style={styles.driverAvatarText}>👨‍🔧</Text>
          </View>
          <View style={{ flex: 1 }}>
            <Text style={styles.driverName}>Mike Rodriguez</Text>
            <Text style={styles.driverTruck}>Unit #4 • Freightliner Flatbed</Text>
          </View>
          <TouchableOpacity
            style={styles.driverCallBtn}
            onPress={() => Alert.alert('Calling Driver', 'Connecting to Driver Unit #4...')}
          >
            <Text style={styles.driverCallText}>📞 CALL</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.trackingLocationRow}>
          <Text style={styles.locationPinIcon}>📍</Text>
          <Text style={styles.locationText} numberOfLines={1}>
            Pickup: 14th St & Broadway, New York, NY
          </Text>
        </View>

        <Button
          title="RETURN TO HOME PORTAL"
          variant="outline"
          onPress={() => setView('home')}
          style={{ marginTop: 14, height: 44 }}
        />
      </View>
    </View>
  );

  switch (view) {
    case 'form':
      return renderForm();
    case 'payment':
      return renderPayment();
    case 'tracking':
      return renderTracking();
    case 'home':
    default:
      return renderHome();
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.background,
  },
  contentContainer: {
    paddingBottom: 32,
  },
  body: {
    paddingHorizontal: 20,
    paddingTop: 20,
  },
  heroCard: {
    backgroundColor: '#1C1215',
    borderRadius: 20,
    borderWidth: 1.5,
    borderColor: COLORS.brandRedBorder,
    padding: 22,
    marginBottom: 28,
  },
  heroTopRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  heroTag: {
    backgroundColor: COLORS.brandRedMuted,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 8,
  },
  heroTagText: {
    color: COLORS.brandRedLight,
    fontWeight: '900',
    fontSize: 10,
    letterSpacing: 0.8,
  },
  heroEta: {
    color: '#FBBF24',
    fontSize: 11,
    fontWeight: '800',
  },
  heroTitle: {
    color: COLORS.textPrimary,
    fontSize: 26,
    fontWeight: '900',
    letterSpacing: 0.5,
  },
  heroSubtitle: {
    color: COLORS.textSecondary,
    fontSize: 13,
    lineHeight: 18,
    marginTop: 6,
    marginBottom: 18,
  },
  heroActionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: 'rgba(239, 68, 68, 0.2)',
    paddingTop: 14,
  },
  heroActionText: {
    color: COLORS.brandRed,
    fontWeight: '800',
    fontSize: 14,
    letterSpacing: 0.5,
  },
  heroActionArrow: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: COLORS.brandRed,
    alignItems: 'center',
    justifyContent: 'center',
  },
  arrowText: {
    color: COLORS.textDark,
    fontWeight: 'bold',
    fontSize: 14,
  },
  sectionTitle: {
    color: COLORS.textMuted,
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 1,
    marginBottom: 12,
    marginLeft: 2,
  },
  serviceGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginBottom: 24,
  },
  serviceTile: {
    width: '48%',
    backgroundColor: COLORS.cardBg,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: COLORS.borderSubtle,
    padding: 16,
    marginBottom: 12,
  },
  serviceTileActive: {
    borderColor: COLORS.brandRedBorder,
    backgroundColor: '#161318',
  },
  serviceIcon: {
    fontSize: 24,
    marginBottom: 8,
  },
  serviceName: {
    color: COLORS.textPrimary,
    fontSize: 14,
    fontWeight: '800',
  },
  serviceSub: {
    color: COLORS.textMuted,
    fontSize: 11,
    marginTop: 2,
  },
  supportCard: {
    marginTop: 6,
  },
  supportRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  supportIconBox: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: 'rgba(239, 68, 68, 0.1)',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  supportIcon: {
    fontSize: 20,
  },
  supportTextColumn: {
    flex: 1,
  },
  supportTitle: {
    color: COLORS.textPrimary,
    fontWeight: '800',
    fontSize: 13,
  },
  supportSub: {
    color: COLORS.textSecondary,
    fontSize: 11,
    marginTop: 2,
  },
  callButton: {
    backgroundColor: COLORS.brandRed,
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 10,
  },
  callButtonText: {
    color: COLORS.textDark,
    fontWeight: '900',
    fontSize: 11,
  },
  cardHeader: {
    color: COLORS.brandRed,
    fontWeight: '900',
    fontSize: 11,
    letterSpacing: 1,
    marginBottom: 14,
  },
  formRow: {
    flexDirection: 'row',
  },
  rateCardHeader: {
    color: COLORS.brandRedLight,
    fontWeight: '900',
    fontSize: 11,
    letterSpacing: 1,
    marginBottom: 14,
  },
  rateBreakdownRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  rateLabel: {
    color: COLORS.textSecondary,
    fontSize: 13,
  },
  rateValue: {
    color: COLORS.textPrimary,
    fontSize: 13,
    fontWeight: '700',
  },
  divider: {
    height: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    marginVertical: 12,
  },
  rateTotalRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  totalLabel: {
    color: COLORS.textPrimary,
    fontWeight: '900',
    fontSize: 14,
    letterSpacing: 0.5,
  },
  totalAmount: {
    color: COLORS.brandRedLight,
    fontWeight: '900',
    fontSize: 22,
  },
  paymentMethodCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: COLORS.cardBg,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: COLORS.borderSubtle,
    padding: 16,
    marginBottom: 12,
  },
  paymentMethodActive: {
    borderColor: COLORS.brandRed,
    backgroundColor: '#161318',
  },
  paymentIcon: {
    fontSize: 24,
    marginRight: 14,
  },
  paymentMethodTitle: {
    color: COLORS.textPrimary,
    fontWeight: '800',
    fontSize: 14,
  },
  paymentMethodSub: {
    color: COLORS.textMuted,
    fontSize: 12,
    marginTop: 2,
  },
  radioCircle: {
    width: 18,
    height: 18,
    borderRadius: 9,
    borderWidth: 2,
    borderColor: COLORS.textMuted,
  },
  radioActive: {
    borderColor: COLORS.brandRed,
    backgroundColor: COLORS.brandRed,
  },
  trackingContainer: {
    flex: 1,
    backgroundColor: COLORS.background,
  },
  mapWrapper: {
    flex: 1,
  },
  trackingBottomCard: {
    position: 'absolute',
    bottom: 20,
    left: 16,
    right: 16,
    backgroundColor: COLORS.cardBg,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: COLORS.borderActive,
    padding: 18,
  },
  trackingHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  trackingEtaBadge: {
    color: '#60A5FA',
    fontWeight: '800',
    fontSize: 11,
  },
  driverProfileRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.borderSubtle,
  },
  driverAvatar: {
    width: 44,
    height: 44,
    borderRadius: 14,
    backgroundColor: COLORS.cardRaised,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  driverAvatarText: {
    fontSize: 22,
  },
  driverName: {
    color: COLORS.textPrimary,
    fontSize: 15,
    fontWeight: '800',
  },
  driverTruck: {
    color: COLORS.textMuted,
    fontSize: 12,
    marginTop: 2,
  },
  driverCallBtn: {
    backgroundColor: 'rgba(239, 68, 68, 0.15)',
    borderWidth: 1,
    borderColor: COLORS.brandRedBorder,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 10,
  },
  driverCallText: {
    color: COLORS.brandRedLight,
    fontWeight: '800',
    fontSize: 11,
  },
  trackingLocationRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 12,
  },
  locationPinIcon: {
    fontSize: 14,
    marginRight: 6,
  },
  locationText: {
    color: COLORS.textSecondary,
    fontSize: 12,
    flex: 1,
  },
});
