package com.sk89q.craftbook.util.jinglenote;

public enum Instrument {
    BANJO,
    BASEDRUM,
    BASS,
    BELL,
    BIT,
    CHIME,
    COW_BELL,
    DIDGERIDOO,
    FLUTE,
    GUITAR,
    HARP,
    HAT,
    IRON_XYLOPHONE,
    PLING,
    SNARE,
    TRUMPET,
    TRUMPET_DISTORTED,
    TROMBONE,
    TROMBONE_DISTORTED,
    XYLOPHONE;

    public static Instrument toMCSound(byte instrument) {
        return switch (instrument) {
            case 1 -> Instrument.BASS;
            case 2 -> Instrument.SNARE;
            case 3 -> Instrument.HAT;
            case 4 -> Instrument.BASEDRUM;
            case 5 -> Instrument.GUITAR;
            case 6 -> Instrument.BELL;
            case 7 -> Instrument.CHIME;
            case 8 -> Instrument.FLUTE;
            case 9 -> Instrument.XYLOPHONE;
            case 10 -> Instrument.PLING;
            case 11 -> Instrument.BANJO;
            case 12 -> Instrument.BIT;
            case 13 -> Instrument.COW_BELL;
            case 14 -> Instrument.DIDGERIDOO;
            case 15 -> Instrument.IRON_XYLOPHONE;
            case 16 -> Instrument.TRUMPET;
            case 17 -> Instrument.TRUMPET_DISTORTED;
            case 18 -> Instrument.TROMBONE;
            case 19 -> Instrument.TROMBONE_DISTORTED;
            default -> Instrument.HARP;
        };
    }
}
