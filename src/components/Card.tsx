import React from 'react';
import { View, StyleSheet, ViewStyle } from 'react-native';
import { COLORS, SHADOWS } from '../theme/colors';

interface CardProps {
  children: React.ReactNode;
  variant?: 'default' | 'raised' | 'highlight';
  style?: ViewStyle;
}

export default function Card({ children, variant = 'default', style }: CardProps) {
  const getVariantStyle = () => {
    switch (variant) {
      case 'raised':
        return styles.raisedCard;
      case 'highlight':
        return styles.highlightCard;
      case 'default':
      default:
        return styles.defaultCard;
    }
  };

  return (
    <View style={[styles.baseCard, getVariantStyle(), SHADOWS.card, style]}>
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  baseCard: {
    borderRadius: 16,
    padding: 18,
    borderWidth: 1,
    borderColor: COLORS.borderSubtle,
    backgroundColor: COLORS.cardBg,
  },
  defaultCard: {
    backgroundColor: COLORS.cardBg,
  },
  raisedCard: {
    backgroundColor: COLORS.cardRaised,
    borderColor: COLORS.borderActive,
  },
  highlightCard: {
    backgroundColor: '#1C1315',
    borderColor: COLORS.brandRedBorder,
  },
});
