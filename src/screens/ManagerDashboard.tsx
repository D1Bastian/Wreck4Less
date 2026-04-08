import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Modal } from 'react-native';
import { COLORS } from '../../App';

export default function ManagerDashboard({ onLogout }: { onLogout: () => void }) {
  const [selectedJob, setSelectedJob] = useState<any>(null);

  const MOCK_QUEUE = [
    { id: 'JOB-9481', vehicle: 'Toyota Camry', location: '14th St & Broadway' },
    { id: 'JOB-9482', vehicle: 'Ford F-150', location: 'I-95 Northbound Marker 44' },
    { id: 'JOB-9483', vehicle: 'Honda Civic', location: '2100 Riverside Dr' },
  ];

  return (
    <View style={styles.container}>
      <Text style={styles.header}>FLEET PORTAL</Text>
      <Text style={{ color: COLORS.brandRed, fontWeight: 'bold', marginBottom: 24 }}>DISPATCH QUEUE</Text>

      <ScrollView>
        {MOCK_QUEUE.map((job) => (
          <View key={job.id} style={styles.jobCard}>
            <Text style={{ color: COLORS.white, fontWeight: 'bold', fontSize: 16 }}>{job.id} • {job.vehicle}</Text>
            <Text style={{ color: COLORS.gray, marginBottom: 12, marginTop: 4 }}>📍 {job.location}</Text>
            <TouchableOpacity style={styles.primaryBtn} onPress={() => setSelectedJob(job)}>
              <Text style={styles.primaryBtnText}>ASSIGN DRIVER</Text>
            </TouchableOpacity>
          </View>
        ))}
      </ScrollView>

      <TouchableOpacity style={styles.logoutBtn} onPress={onLogout}>
        <Text style={styles.logoutText}>SIGN OUT OF FLEET</Text>
      </TouchableOpacity>

      <Modal visible={!!selectedJob} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>ASSIGN DRIVER TO {selectedJob?.id}</Text>
            
            <TouchableOpacity style={styles.modalOption} onPress={() => setSelectedJob(null)}>
              <Text style={{ color: COLORS.white, fontWeight: 'bold' }}>🚚 Driver: Mike (Unit 4)</Text>
              <Text style={{ color: COLORS.gray }}>Est. Rate: $195</Text>
            </TouchableOpacity>
            
            <TouchableOpacity style={styles.modalOption} onPress={() => setSelectedJob(null)}>
              <Text style={{ color: COLORS.white, fontWeight: 'bold' }}>🚚 Driver: Sarah (Unit 2)</Text>
              <Text style={{ color: COLORS.gray }}>Est. Rate: $195</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.logoutBtn} onPress={() => setSelectedJob(null)}>
              <Text style={styles.logoutText}>CANCEL</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, backgroundColor: COLORS.pureBlack, paddingTop: 60 },
  header: { fontSize: 24, fontWeight: '900', color: '#FFFFFF' },
  jobCard: { backgroundColor: COLORS.cardGray, padding: 20, borderRadius: 16, marginBottom: 16 },
  primaryBtn: { backgroundColor: COLORS.brandRed, padding: 12, borderRadius: 12, alignItems: 'center' },
  primaryBtnText: { color: COLORS.pureBlack, fontWeight: '900' },
  logoutBtn: { alignSelf: 'center', padding: 12, marginTop: 12 },
  logoutText: { color: COLORS.gray, fontWeight: '900' },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.8)', justifyContent: 'center', padding: 24 },
  modalContent: { backgroundColor: COLORS.cardGray, padding: 24, borderRadius: 24 },
  modalTitle: { color: COLORS.brandRed, fontWeight: '900', fontSize: 18, marginBottom: 20 },
  modalOption: { backgroundColor: COLORS.pureBlack, padding: 16, borderRadius: 12, marginBottom: 12 },
});
