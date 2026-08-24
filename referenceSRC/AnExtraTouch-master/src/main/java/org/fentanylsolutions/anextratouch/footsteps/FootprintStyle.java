package org.fentanylsolutions.anextratouch.footsteps;

public enum FootprintStyle {

    SHOE,
    SQUARE,
    HORSESHOE,
    BIRD,
    PAW,
    SQUARE_SOLID,
    LOWRES_SQUARE;

    public static FootprintStyle getStyle(int v) {
        if (v >= values().length) {
            return LOWRES_SQUARE;
        }
        return values()[v];
    }
}
