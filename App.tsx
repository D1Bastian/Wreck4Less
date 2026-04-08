import React, { useState } from 'react';
import { SafeAreaView, StatusBar, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import CustomerDashboard from './src/screens/CustomerDashboard';
import ManagerDashboard from './src/screens/ManagerDashboard';
import DriverDashboard from './src/screens/DriverDashboard';

export const COLORS = {
  brandRed: '#FF0000',
  pureBlack: '#000000',
  surfaceZinc: '#121212',
  cardGray: '#1C1C1E',
  white: '#FFFFFF',
  gray: '#888888',
};

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
          <View style={styles.landingContainer}>
            <View style={styles.header}>
              <Text style={styles.appTitle}>WRECK4LESS</Text>
              <Text style={styles.appSubtitle}>TESTING CONSOLE</Text>
            </View>

            <TouchableOpacity style={styles.roleButton} onPress={() => setCurrentRole('CUSTOMER')}>
              <Text style={styles.roleButtonText}>LOGIN AS CUSTOMER</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.roleButton} onPress={() => setCurrentRole('MANAGER')}>
              <Text style={styles.roleButtonText}>LOGIN AS MANAGER</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.roleButton} onPress={() => setCurrentRole('DRIVER')}>
              <Text style={styles.roleButtonText}>LOGIN AS DRIVER</Text>
            </TouchableOpacity>
          </View>
        );
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor={COLORS.pureBlack} />
      {renderDashboard()}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.pureBlack,
  },
  landingContainer: {
    flex: 1,
    justifyContent: 'center',
    padding: 32,
  },
  header: {
    alignItems: 'center',
    marginBottom: 64,
  },
  appTitle: {
    fontSize: 32,
    fontWeight: '900',
    color: '#FFF',
    fontStyle: 'italic',
  },
  appSubtitle: {
    fontSize: 12,
    fontWeight: 'bold',
    color: 'rgba(255,0,0,0.6)',
    marginTop: 4,
  },
  roleButton: {
    backgroundColor: COLORS.brandRed,
    borderRadius: 16,
    paddingVertical: 20,
    marginBottom: 24,
    alignItems: 'center',
  },
  roleButtonText: {
    color: COLORS.pureBlack,
    fontWeight: '900',
    fontSize: 16,
  },
});

export default App;
