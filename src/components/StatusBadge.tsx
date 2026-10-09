import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { COLORS } from '../theme/colors';

interface StatusBadgeProps {
  status: 'ONLINE' | 'OFFLINE' | 'EN_ROUTE' | 'ON_SITE' | 'COMPLETED' | 'PENDING' | 'AWAITING_OPS';
  label?: string;
}

export default function StatusBadge({ status, label }: StatusBadgeProps) {
  const getStatusConfig = () => {
    switch (status) {
      case 'ONLINE':
      case 'COMPLETED':
        return {
          bg: COLORS.statusGreenMuted,
          border: COLORS.statusGreen,
          text: COLORS.statusGreen,
          dot: COLORS.statusGreen,
          defaultLabel: status === 'ONLINE' ? 'ONLINE' : 'COMPLETED',
        };
      case 'EN_ROUTE':
      case 'ON_SITE':
        return {
          bg: COLORS.statusBlueMuted,
          border: COLORS.statusBlue,
          text: COLORS.statusBlue,
          dot: COLORS.statusBlue,
          defaultLabel: status === 'EN_ROUTE' ? 'EN ROUTE' : 'ON SITE',
        };
      case 'PENDING':
      case 'AWAITING_OPS':
        return {
          bg: COLORS.statusYellowMuted,
          border: COLORS.statusYellow,
          text: COLORS.statusYellow,
          dot: COLORS.statusYellow,
          defaultLabel: status === 'PENDING' ? 'PENDING' : 'AWAITING OPS',
        };
      case 'OFFLINE':
      default:
        return {
          bg: 'rgba(113, 113, 122, 0.15)',
          border: COLORS.textMuted,
          text: COLORS.textMuted,
          dot: COLORS.textMuted,
          defaultLabel: 'OFFLINE',
        };
    }
  };

  const config = getStatusConfig();

  return (
    <View style={[styles.badge, { backgroundColor: config.bg, borderColor: config.border }]}>
      <View style={[styles.dot, { backgroundColor: config.dot }]} />
      <Text style={[styles.text, { color: config.text }]}>
        {label || config.defaultLabel}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 20,
    borderWidth: 1,
    alignSelf: 'flex-start',
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    marginRight: 6,
  },
  text: {
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
});
