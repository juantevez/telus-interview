package com.namegame.domain.port.in;

import com.namegame.domain.model.Game;

public interface StartGamePort {

    Game startGame(int totalRounds, int facesPerRound);
}
