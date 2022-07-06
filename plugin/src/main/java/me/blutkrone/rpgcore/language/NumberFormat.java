package me.blutkrone.rpgcore.language;

import java.util.Locale;

public enum NumberFormat {
    INTEGER(0) {
        @Override
        public String translate(double number, int precision) {
            return String.valueOf(((int) number));
        }
    },
    PERCENT(0) {
        @Override
        public String translate(double number, int precision) {
            return String.format(Locale.US, "%." + precision + "f", number * 100f) + "%";
        }
    },
    DECIMAL(2) {
        @Override
        public String translate(double number, int precision) {
            return String.format(Locale.US, "%." + precision + "f", number);
        }
    },
    MULTIPLIER(2) {
        @Override
        public String translate(double number, int precision) {
            if (number <= 0d) {
                return String.format(Locale.US, "%." + precision + "fx", Math.max(0d, 1d + number));
            } else {
                return String.format(Locale.US, "%." + precision + "fx", number);
            }
        }
    },
    QUESTION(0) {
        @Override
        public String translate(double number, int precision) {
            return number >= 1d ? "Yes" : "No";
        }
    },
    BOOLEAN(0) {
        @Override
        public String translate(double number, int precision) {
            return number >= 1d ? "True" : "False";
        }
    },
    TIME(0) {
        @Override
        public String translate(double number, int precision) {
            return String.format(Locale.US, "%." + precision + "f", number / 20d) + "s";
        }
    };

    public final int precision;

    NumberFormat(int precision) {
        this.precision = precision;
    }

    public abstract String translate(double number, int precision);
}
