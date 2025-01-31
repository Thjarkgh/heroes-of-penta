import { Boot } from './scenes/Boot';
import { MainMenu } from './scenes/MainMenu';
import { AUTO, Game, Scale } from 'phaser';
import { Preloader } from './scenes/Preloader';

//  Find out more information about the Game Config at:
//  https://newdocs.phaser.io/docs/3.70.0/Phaser.Types.Core.GameConfig
const config: Phaser.Types.Core.GameConfig = {
  type: AUTO,
  width: 1024,
  height: 576 + 64,
  scale: {
    // Fit to window
    mode: Scale.FIT,
    // Center vertically and horizontally
    autoCenter: Scale.CENTER_HORIZONTALLY
  },
  parent: 'game-container',
  backgroundColor: '#000000',
  scene: [
    Boot,
    Preloader,
    MainMenu,
  ],
  expandParent: true,
  dom: {
    createContainer: true
  }
};

const StartGame = (parent: string) => {
  const game = new Game({ ...config, parent });
  return game;
}

export default StartGame;
