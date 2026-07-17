package com.sk89q.craftbook.util.jinglenote.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.craftbook.util.jinglenote.Instrument;
import com.sk89q.craftbook.util.jinglenote.JingleNotePlayer;
import com.sk89q.craftbook.util.jinglenote.JingleSequencer;
import com.sk89q.craftbook.util.jinglenote.JingleSequencer.Note;

public class BukkitJingleNotePlayer extends JingleNotePlayer {

    public BukkitJingleNotePlayer(String player, JingleSequencer seq, SearchArea area) {
        super(player, seq, area);
    }

    private Player p = null;

    @Override
    public void play(Note note) {

        if (!isPlaying())
            return;

        p.playSound(p.getLocation(), toSound(note.getInstrument()), SoundCategory.RECORDS, note.getVelocity(),
                note.getNote());
    }

    @Override
    public boolean isPlaying() {

        if (p == null || !p.isOnline()) {
            p = Bukkit.getPlayerExact(player);
        }
        return !(p == null || !p.isOnline() || area != null && !area.isWithinArea(p.getLocation()))
                && super.isPlaying();

    }

    private static Sound toSound(Instrument instrument) {
        return switch (instrument) {
            case BASS -> Sound.BLOCK_NOTE_BLOCK_BASS;
            case SNARE -> Sound.BLOCK_NOTE_BLOCK_SNARE;
            case HAT -> Sound.BLOCK_NOTE_BLOCK_HAT;
            case BANJO -> Sound.BLOCK_NOTE_BLOCK_BANJO;
            case BASEDRUM -> Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case BELL -> Sound.BLOCK_NOTE_BLOCK_BELL;
            case BIT -> Sound.BLOCK_NOTE_BLOCK_BIT;
            case CHIME -> Sound.BLOCK_NOTE_BLOCK_CHIME;
            case COW_BELL -> Sound.BLOCK_NOTE_BLOCK_COW_BELL;
            case DIDGERIDOO -> Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case FLUTE -> Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case XYLOPHONE -> Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case IRON_XYLOPHONE -> Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
            case PLING -> Sound.BLOCK_NOTE_BLOCK_PLING;
            case GUITAR -> Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case TRUMPET -> Sound.BLOCK_NOTE_BLOCK_TRUMPET;
            case TRUMPET_DISTORTED -> Sound.BLOCK_NOTE_BLOCK_TRUMPET_EXPOSED;
            case TROMBONE -> Sound.BLOCK_NOTE_BLOCK_TRUMPET_OXIDIZED;
            case TROMBONE_DISTORTED -> Sound.BLOCK_NOTE_BLOCK_TRUMPET_WEATHERED;
            default -> Sound.BLOCK_NOTE_BLOCK_HARP;
        };
    }
}
