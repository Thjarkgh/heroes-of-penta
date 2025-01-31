import { GameObjects, Scene } from 'phaser';

import { EventBus } from '../EventBus';
import { FormUtil } from '../components/formUtil';

export class MainMenu extends Scene {
  background: GameObjects.Image;
  logo: GameObjects.Image;
  title: GameObjects.Image;
  thankYou: GameObjects.Image;
  logoTween: Phaser.Tweens.Tween | null;
  formUtil: FormUtil;
  button: GameObjects.Sprite;
  errorMessage: GameObjects.Text;

  constructor() {
    super('MainMenu');
  }

  preload() { }

  update() { }

  subscribe() {
    const editBox = document.getElementById("subscriberEmail");
    const email = (editBox as any)?.value || "";
    this.errorMessage.setVisible(false);

    fetch("/subscribe", {
      method: "POST",
      headers: { "Content-Type": "application/JSON" },
      body: JSON.stringify({ email })
    })
      .then((value) => {
        value.text().then((txt) => {
          if (value.ok) {
            if (txt === "ok") {
              this.button.setVisible(false);
              editBox?.remove();
              this.logo.setVisible(false);
              this.thankYou.setVisible(true);
            } else {
              window.location.replace(txt);
            }
          } else {
            this.errorMessage.setText(txt).setVisible(true);
          }
        })
      })
      .catch((reason) => {
        this.errorMessage.setText(reason.toString()).setVisible(true);
      })
  }

  create() {
    this.background = this.add.image(512, 576 / 2, 'banner');

    const titleHeight = 180;
    const titleWidth = 470;
    this.title = this.add.image(512, titleHeight / 2 + 3, 'title').setDepth(100).setDisplaySize(titleWidth, titleHeight);
    this.logo = this.add.image(512, 576 / 2, 'comingSoon').setDepth(100);
    this.thankYou = this.add.image(512, 576 / 2, 'thankYou').setVisible(false);
    this.errorMessage = this.add.text(512, 576 / 2, "", { align: "center", fontFamily: "serif", fontSize: "15", backgroundColor: "#000000" }).setVisible(false);

    this.formUtil = new FormUtil({
      scene: this,
      rows: 10,
      cols: 13
    });

    // this.formUtil.showNumbers();
    // this.formUtil.scaleToGameW("myText", .3);
    // this.formUtil.placeElementAt(16, 'myText', true);
    
    const editBoxPos = this.formUtil.alignGrid.getPosByIndex(9 * 13 + 3);
    const editBox = this.add.dom(editBoxPos.x, editBoxPos.y, "#subscriberEmail");
    this.formUtil.scaleToGameW("subscriberEmail", .5);
    // this.formUtil.scaleToGameH("subscriberEmail", 48 / (576+64));
    // this.formUtil.placeElementAt(9 * 13 + 3, "subscriberEmail", false, false);
    // this.formUtil.addChangeCallback("subscriberEmail", this.textAreaChanged, this as any);

    const buttonPos = this.formUtil.alignGrid.getPosByIndex(9 * 13 + 10);
    this.button = this.add.sprite(buttonPos.x, buttonPos.y, "subscribeButton").setInteractive(); //.addListener("click", this.subscribe);
    this.button.setScale(this.formUtil.alignGrid.ch / this.button.height);
    this.button.on(Phaser.Input.Events.POINTER_DOWN, () => {
      this.button.setTexture('subscribeButtonPressed');
    });
    this.button.on(Phaser.Input.Events.POINTER_UP, () => {
      if (this.button.texture.key == 'subscribeButtonPressed') {
        this.subscribe();
        this.button.setTexture('subscribeButton');
      }
    });
    this.button.on(Phaser.Input.Events.GAMEOBJECT_POINTER_OUT, () => {
      if (this.button.texture.key == 'subscribeButtonPressed') {
        this.button.setTexture('subscribeButton');
      }
    });
    
    // this.title = this.add.text(512, 460, 'Main Menu', {
    //     fontFamily: 'Arial Black', fontSize: 38, color: '#ffffff',
    //     stroke: '#000000', strokeThickness: 8,
    //     align: 'center'
    // }).setOrigin(0.5).setDepth(100);

    EventBus.emit('current-scene-ready', this);
  }

  changeScene() {
    if (this.logoTween) {
      this.logoTween.stop();
      this.logoTween = null;
    }

    this.scene.start('Game');
  }

  moveLogo(vueCallback: ({ x, y }: { x: number, y: number }) => void) {
    if (this.logoTween) {
      if (this.logoTween.isPlaying()) {
        this.logoTween.pause();
      }
      else {
        this.logoTween.play();
      }
    }
    else {
      this.logoTween = this.tweens.add({
        targets: this.logo,
        x: { value: 750, duration: 3000, ease: 'Back.easeInOut' },
        y: { value: 80, duration: 1500, ease: 'Sine.easeOut' },
        yoyo: true,
        repeat: -1,
        onUpdate: () => {
          if (vueCallback) {
            vueCallback({
              x: Math.floor(this.logo.x),
              y: Math.floor(this.logo.y)
            });
          }
        }
      });
    }
  }
}
