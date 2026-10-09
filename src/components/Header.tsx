import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { COLORS } from '../theme/colors';

interface HeaderProps {
  title: string;
  subtitle?: string;
  roleBadge?: string;
  onBack?: () => void;
  onLogout?: () => void;
}

export default function Header({ title, subtitle, roleBadge, onBack, onLogout }: HeaderProps) {
  return (
    <View style={styles.headerContainer}>
      <View style={styles.titleRow}>
        {onBack && (
          <TouchableOpacity style={styles.backButton} onPress={onBack} activeOpacity={0.7}>
            <Text style={styles.backIcon}>←</Text>
          </TouchableOpacity>
        )}
        <View style={styles.textColumn}>
          <View style={styles.tagRow}>
            <Text style={styles.brandTitle}>WRECK4LESS</Text>
            {roleBadge && (
              <View style={styles.roleTag}>
                <Text style={styles.roleTagText}>{roleBadge}</Text>
              </View>
            )}
          </View>
          <Text style={styles.mainTitle}>{title}</Text>
          {subtitle && <Text style={styles.subtitle}>{subtitle}</Text>}
        </View>

        {onLogout && (
          <TouchableOpacity style={styles.logoutButton} onPress={onLogout} activeOpacity={0.7}>
            <Text style={styles.logoutText}>EXIT</Text>
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  headerContainer: {
    paddingHorizontal: 20,
    paddingTop: 16,
    paddingBottom: 16,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.borderSubtle,
    backgroundColor: COLORS.background,
  },
  titleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  backButton: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: COLORS.cardBg,
    borderWidth: 1,
    borderColor: COLORS.borderSubtle,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  backIcon: {
    color: COLORS.textPrimary,
    fontSize: 18,
    fontWeight: 'bold',
  },
  textColumn: {
    flex: 1,
  },
  tagRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 2,
  },
  brandTitle: {
    color: COLORS.brandRed,
    fontWeight: '900',
    fontSize: 12,
    letterSpacing: 1.2,
  },
  roleTag: {
    backgroundColor: COLORS.brandRedMuted,
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 6,
    marginLeft: 8,
    borderWidth: 1,
    borderColor: COLORS.brandRedBorder,
  },
  roleTagText: {
    color: COLORS.brandRedLight,
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  mainTitle: {
    color: COLORS.textPrimary,
    fontSize: 22,
    fontWeight: '900',
    letterSpacing: 0.3,
  },
  subtitle: {
    color: COLORS.textSecondary,
    fontSize: 12,
    marginTop: 2,
  },
  logoutButton: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 10,
    backgroundColor: COLORS.cardBg,
    borderWidth: 1,
    borderColor: COLORS.borderSubtle,
  },
  logoutText: {
    color: COLORS.textMuted,
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
});
