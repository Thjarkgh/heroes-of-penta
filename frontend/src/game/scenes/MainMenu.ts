import { GameObjects, Scene } from 'phaser';

import { EventBus } from '../EventBus';
import { FormUtil } from '../components/formUtil';

export class MainMenu extends Scene
{
    background: GameObjects.Image;
    logo: GameObjects.Image;
    title: GameObjects.Text;
    logoTween: Phaser.Tweens.Tween | null;
    formUtil: FormUtil;

    // email: string = "";
    // buttonPressed: boolean = false;
    // button: Phaser.GameObjects.Sprite;

    constructor ()
    {
        super('MainMenu');
    }

    preload() {}
    
    // textAreaChanged() {
    //     this.email = this.formUtil.getTextAreaValue("area51");
    // }
    update() {}

    subscribe() {
        const editBox = document.getElementById("subscriberEmail");
        const email = (editBox as any)?.value || "";
        console.log(email);
    }

    // pointerDownSubscribeButton() {
    //     this.buttonPressed = true;
    //     this.button.setTexture('subscribeButtonPressed');
    // }

    // pointerUpSubscribeButton() {
    //     if (this.buttonPressed) {
    //         // click
    //         console.log(this.email);
    //     }
    //     this.pointerUpOutsideSubscribeButton();
    // }

    // pointerUpOutsideSubscribeButton() {
    //     this.buttonPressed = false;
    //     this.button.setTexture('subscribeButton');
    // }

    create ()
    {
        this.background = this.add.image(512, 576/2, 'banner');

        const titleHeight = 180;
        const titleWidth = 470;
        this.logo = this.add.image(512, titleHeight / 2 + 3, 'title').setDepth(100).setDisplaySize(titleWidth, titleHeight);
        this.logo = this.add.image(512, 576/2, 'comingSoon').setDepth(100);

        
        this.formUtil = new FormUtil({
            scene: this,
            rows: 10,
            cols: 13
        });
        // this.formUtil.showNumbers();
        //
        //
        //
        // this.formUtil.scaleToGameW("myText", .3);
        // this.formUtil.placeElementAt(16, 'myText', true);
        // //
        //
        //
        const editBoxPos = this.formUtil.alignGrid.getPosByIndex(9 * 13 + 3);
        const editBox = this.add.dom(editBoxPos.x, editBoxPos.y, "#subscriberEmail");
        this.formUtil.scaleToGameW("subscriberEmail", .5);
        // this.formUtil.scaleToGameH("subscriberEmail", 48 / (576+64));
        // this.formUtil.placeElementAt(9 * 13 + 3, "subscriberEmail", false, false);
        // this.formUtil.addChangeCallback("subscriberEmail", this.textAreaChanged, this as any);

        const buttonPos = this.formUtil.alignGrid.getPosByIndex(9 * 13 + 10);
        const button = this.add.sprite(buttonPos.x, buttonPos.y, "subscribeButton").setInteractive(); //.addListener("click", this.subscribe);
        button.setScale(this.formUtil.alignGrid.ch / button.height);
        button.on(Phaser.Input.Events.POINTER_DOWN, () => {
            button.setTexture('subscribeButtonPressed');
        });
        button.on(Phaser.Input.Events.POINTER_UP, () => {
            if (button.texture.key == 'subscribeButtonPressed') {
                this.subscribe();
                button.setTexture('subscribeButton');
            }
        });
        button.on(Phaser.Input.Events.GAMEOBJECT_POINTER_OUT, () => {
            if (button.texture.key == 'subscribeButtonPressed') {
                button.setTexture('subscribeButton');
            }
        });

        // const editBox = this.add.text(1024 / 13 + 1025 / 13 * 2.5)
        //
        //
        //
        // this.title = this.add.text(512, 460, 'Main Menu', {
        //     fontFamily: 'Arial Black', fontSize: 38, color: '#ffffff',
        //     stroke: '#000000', strokeThickness: 8,
        //     align: 'center'
        // }).setOrigin(0.5).setDepth(100);

        EventBus.emit('current-scene-ready', this);
    }
    
    changeScene ()
    {
        if (this.logoTween)
        {
            this.logoTween.stop();
            this.logoTween = null;
        }

        this.scene.start('Game');
    }

    moveLogo (vueCallback: ({ x, y }: { x: number, y: number }) => void)
    {
        if (this.logoTween)
        {
            if (this.logoTween.isPlaying())
            {
                this.logoTween.pause();
            }
            else
            {
                this.logoTween.play();
            }
        } 
        else
        {
            this.logoTween = this.tweens.add({
                targets: this.logo,
                x: { value: 750, duration: 3000, ease: 'Back.easeInOut' },
                y: { value: 80, duration: 1500, ease: 'Sine.easeOut' },
                yoyo: true,
                repeat: -1,
                onUpdate: () => {
                    if (vueCallback)
                    {
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
