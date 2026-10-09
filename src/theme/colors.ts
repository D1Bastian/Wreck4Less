export const COLORS = {
  // Brand accents
  brandRed: '#EF4444',       // Crimson red accent
  brandRedDark: '#DC2626',   // Deep crimson
  brandRedLight: '#F87171',  // Bright highlight red
  brandRedMuted: 'rgba(239, 68, 68, 0.15)', // Tinted glow/surface
  brandRedBorder: 'rgba(239, 68, 68, 0.35)',

  // Dark backgrounds & surfaces
  background: '#09090B',     // Deep obsidian background
  surfaceZinc: '#121216',    // Secondary surface
  cardBg: '#18181F',         // Elevated card background
  cardRaised: '#22222C',     // High-elevation surface
  inputBg: '#131318',        // Input field fill

  // Borders & Dividers
  borderSubtle: 'rgba(255, 255, 255, 0.08)',
  borderActive: 'rgba(255, 255, 255, 0.18)',

  // Typography
  textPrimary: '#FFFFFF',
  textSecondary: '#A1A1AA',
  textMuted: '#71717A',
  textDark: '#09090B',

  // Status colors
  statusGreen: '#10B981',    // Online / Success
  statusGreenMuted: 'rgba(16, 185, 129, 0.15)',
  statusYellow: '#F59E0B',   // Pending / Standby
  statusYellowMuted: 'rgba(245, 158, 11, 0.15)',
  statusBlue: '#3B82F6',     // En Route / Dispatched
  statusBlueMuted: 'rgba(59, 130, 246, 0.15)',
  statusRed: '#EF4444',
  
  // Legacy aliases for backward compatibility
  pureBlack: '#09090B',
  cardGray: '#18181F',
  white: '#FFFFFF',
  gray: '#A1A1AA',
};

export const SHADOWS = {
  subtle: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 6,
    elevation: 3,
  },
  card: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.35,
    shadowRadius: 12,
    elevation: 6,
  },
  glowRed: {
    shadowColor: '#EF4444',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.35,
    shadowRadius: 10,
    elevation: 8,
  },
};
