import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Modal, Alert } from 'react-native';
import { COLORS } from '../theme/colors';
import Header from '../components/Header';
import Button from '../components/Button';
import Card from '../components/Card';
import StatusBadge from '../components/StatusBadge';

interface ManagerDashboardProps {
  onLogout: () => void;
}

interface DispatchJob {
  id: string;
  vehicle: string;
  location: string;
  status: 'AWAITING_OPS' | 'PENDING' | 'EN_ROUTE';
  rate: string;
  timeAgo: string;
  notes: string;
}

export default function ManagerDashboard({ onLogout }: ManagerDashboardProps) {
  const [selectedJob, setSelectedJob] = useState<DispatchJob | null>(null);

  const [queue, setQueue] = useState<DispatchJob[]>([
    {
      id: 'WRK-9481',
      vehicle: '2021 Toyota Camry',
      location: '14th St & Broadway, NY',
      status: 'AWAITING_OPS',
      rate: '$145.00',
      timeAgo: '4m ago',
      notes: 'Front overheating, unable to steer',
    },
    {
      id: 'WRK-9482',
      vehicle: '2019 Ford F-150',
      location: 'I-95 Northbound Marker 44',
      status: 'PENDING',
      rate: '$210.00',
      timeAgo: '12m ago',
      notes: 'Highway shoulder rollover, requires flatbed with winch',
    },
    {
      id: 'WRK-9483',
      vehicle: '2022 Honda Civic',
      location: '2100 Riverside Dr',
      status: 'AWAITING_OPS',
      rate: '$115.00',
      timeAgo: '18m ago',
      notes: 'Dead battery & steering lock',
    },
  ]);

  const DRIVERS = [
    { id: 'drv-1', name: 'Mike Rodriguez', unit: 'Unit #4 (Flatbed)', dist: '1.2 mi away', status: 'AVAILABLE' },
    { id: 'drv-2', name: 'Sarah Chen', unit: 'Unit #2 (Wheel-Lift)', dist: '2.8 mi away', status: 'AVAILABLE' },
    { id: 'drv-3', name: 'David Miller', unit: 'Unit #7 (Heavy Wrecker)', dist: '4.5 mi away', status: 'ON_CALL' },
  ];

  const handleAssign = (driverName: string, unit: string) => {
    if (!selectedJob) return;
    Alert.alert(
      'Driver Dispatched',
      `Assigned ${driverName} (${unit}) to dispatch ${selectedJob.id}. Push notification dispatched.`
    );
    setQueue(prev =>
      prev.map(j => (j.id === selectedJob.id ? { ...j, status: 'EN_ROUTE' as const } : j))
    );
    setSelectedJob(null);
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      <Header
        title="Fleet Command"
        subtitle="Live Operations & Telematics"
        roleBadge="DISPATCHER"
        onLogout={onLogout}
      />

      <View style={styles.body}>
        {/* KPI Metrics Summary Row */}
        <View style={styles.kpiRow}>
          <Card variant="raised" style={styles.kpiCard}>
            <Text style={styles.kpiLabel}>ACTIVE CALLS</Text>
            <Text style={[styles.kpiValue, { color: COLORS.brandRedLight }]}>3</Text>
            <Text style={styles.kpiSub}>1 Pending Auth</Text>
          </Card>

          <Card variant="raised" style={styles.kpiCard}>
            <Text style={styles.kpiLabel}>UNITS ONLINE</Text>
            <Text style={[styles.kpiValue, { color: COLORS.statusGreen }]}>5 / 8</Text>
            <Text style={styles.kpiSub}>2 Available</Text>
          </Card>

          <Card variant="raised" style={styles.kpiCard}>
            <Text style={styles.kpiLabel}>AVG DISPATCH</Text>
            <Text style={[styles.kpiValue, { color: COLORS.statusBlue }]}>11m</Text>
            <Text style={styles.kpiSub}>-2m vs target</Text>
          </Card>
        </View>

        {/* Section Header */}
        <View style={styles.sectionHeaderRow}>
          <Text style={styles.sectionTitle}>PRIORITY DISPATCH QUEUE</Text>
          <Text style={styles.queueCountBadge}>{queue.length} ACTIVE</Text>
        </View>

        {/* Job Queue List */}
        {queue.map(job => (
          <Card key={job.id} variant="raised" style={styles.jobCard}>
            <View style={styles.jobTopRow}>
              <View>
                <Text style={styles.jobId}>{job.id}</Text>
                <Text style={styles.jobVehicle}>{job.vehicle}</Text>
              </View>
              <View style={{ alignItems: 'flex-end' }}>
                <StatusBadge status={job.status} />
                <Text style={styles.timeAgoText}>{job.timeAgo}</Text>
              </View>
            </View>

            <View style={styles.jobInfoBox}>
              <View style={styles.infoLine}>
                <Text style={styles.infoIcon}>📍</Text>
                <Text style={styles.infoLocation} numberOfLines={1}>{job.location}</Text>
              </View>
              <View style={styles.infoLine}>
                <Text style={styles.infoIcon}>⚠️</Text>
                <Text style={styles.infoNotes} numberOfLines={2}>{job.notes}</Text>
              </View>
            </View>

            <View style={styles.jobFooterRow}>
              <Text style={styles.rateText}>Approved Rate: <Text style={{ color: COLORS.textPrimary }}>{job.rate}</Text></Text>
              <Button
                title={job.status === 'EN_ROUTE' ? 'VIEW ROUTE' : 'ASSIGN UNIT'}
                variant={job.status === 'EN_ROUTE' ? 'secondary' : 'primary'}
                onPress={() => setSelectedJob(job)}
                style={styles.assignBtn}
                textStyle={{ fontSize: 12 }}
              />
            </View>
          </Card>
        ))}
      </View>

      {/* Driver Assignment Modal */}
      <Modal visible={!!selectedJob} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeaderRow}>
              <View>
                <Text style={styles.modalSub}>DISPATCH REASSIGNMENT</Text>
                <Text style={styles.modalTitle}>ASSIGN UNIT TO {selectedJob?.id}</Text>
              </View>
              <TouchableOpacity onPress={() => setSelectedJob(null)} style={styles.modalCloseBtn}>
                <Text style={styles.modalCloseText}>✕</Text>
              </TouchableOpacity>
            </View>

            <Text style={styles.modalSectionLabel}>AVAILABLE DRIVER ROSTER</Text>

            {DRIVERS.map(drv => (
              <TouchableOpacity
                key={drv.id}
                style={styles.driverOptionCard}
                onPress={() => handleAssign(drv.name, drv.unit)}
              >
                <View style={styles.driverAvatarCircle}>
                  <Text style={{ fontSize: 18 }}>🚚</Text>
                </View>
                <View style={{ flex: 1 }}>
                  <Text style={styles.driverOptionName}>{drv.name}</Text>
                  <Text style={styles.driverOptionUnit}>{drv.unit} • {drv.dist}</Text>
                </View>
                <View style={styles.driverSelectPill}>
                  <Text style={styles.driverSelectText}>ASSIGN ›</Text>
                </View>
              </TouchableOpacity>
            ))}

            <Button
              title="DISMISS"
              variant="outline"
              onPress={() => setSelectedJob(null)}
              style={{ marginTop: 14 }}
            />
          </View>
        </View>
      </Modal>
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
  kpiRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 24,
  },
  kpiCard: {
    width: '31%',
    padding: 12,
    alignItems: 'center',
  },
  kpiLabel: {
    color: COLORS.textMuted,
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  kpiValue: {
    fontSize: 20,
    fontWeight: '900',
    marginVertical: 4,
  },
  kpiSub: {
    color: COLORS.textSecondary,
    fontSize: 9,
  },
  sectionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
  },
  sectionTitle: {
    color: COLORS.textMuted,
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 1,
  },
  queueCountBadge: {
    color: COLORS.brandRedLight,
    backgroundColor: COLORS.brandRedMuted,
    fontSize: 10,
    fontWeight: '900',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: COLORS.brandRedBorder,
  },
  jobCard: {
    marginBottom: 16,
  },
  jobTopRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  jobId: {
    color: COLORS.brandRed,
    fontWeight: '900',
    fontSize: 12,
    letterSpacing: 0.8,
  },
  jobVehicle: {
    color: COLORS.textPrimary,
    fontWeight: '800',
    fontSize: 15,
    marginTop: 2,
  },
  timeAgoText: {
    color: COLORS.textMuted,
    fontSize: 10,
    marginTop: 4,
  },
  jobInfoBox: {
    backgroundColor: COLORS.inputBg,
    borderRadius: 10,
    padding: 10,
    marginBottom: 12,
  },
  infoLine: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 3,
  },
  infoIcon: {
    fontSize: 13,
    marginRight: 8,
  },
  infoLocation: {
    color: COLORS.textSecondary,
    fontSize: 12,
    flex: 1,
  },
  infoNotes: {
    color: COLORS.textMuted,
    fontSize: 11,
    flex: 1,
  },
  jobFooterRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  rateText: {
    color: COLORS.textMuted,
    fontSize: 12,
    fontWeight: '600',
  },
  assignBtn: {
    height: 38,
    paddingHorizontal: 16,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(9, 9, 11, 0.85)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: COLORS.cardBg,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 24,
    borderTopWidth: 1,
    borderColor: COLORS.borderActive,
  },
  modalHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 18,
  },
  modalSub: {
    color: COLORS.brandRed,
    fontSize: 10,
    fontWeight: '900',
    letterSpacing: 1,
  },
  modalTitle: {
    color: COLORS.textPrimary,
    fontSize: 18,
    fontWeight: '900',
    marginTop: 2,
  },
  modalCloseBtn: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: COLORS.cardRaised,
    alignItems: 'center',
    justifyContent: 'center',
  },
  modalCloseText: {
    color: COLORS.textSecondary,
    fontSize: 14,
    fontWeight: 'bold',
  },
  modalSectionLabel: {
    color: COLORS.textMuted,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 0.8,
    marginBottom: 12,
  },
  driverOptionCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: COLORS.cardRaised,
    borderRadius: 14,
    padding: 14,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: COLORS.borderSubtle,
  },
  driverAvatarCircle: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: COLORS.inputBg,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  driverOptionName: {
    color: COLORS.textPrimary,
    fontWeight: '800',
    fontSize: 14,
  },
  driverOptionUnit: {
    color: COLORS.textSecondary,
    fontSize: 12,
    marginTop: 2,
  },
  driverSelectPill: {
    backgroundColor: COLORS.brandRedMuted,
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: COLORS.brandRedBorder,
  },
  driverSelectText: {
    color: COLORS.brandRedLight,
    fontWeight: '900',
    fontSize: 10,
  },
});
