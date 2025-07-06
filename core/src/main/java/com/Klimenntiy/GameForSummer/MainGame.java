package com.Klimenntiy.GameForSummer;

import com.badlogic.gdx.Game;
import screens.MenuScreen;

public class MainGame extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
