import { toNum } from "./common";

export interface IAlignGridConfig {
    scene: Phaser.Scene | undefined;
    rows: number | undefined;
    cols: number | undefined;
    width?: number | undefined;
    height?: number | undefined;
}

export class AlignGrid {
    h: number;
    w: number;
    rows: number;
    cols: number;
    scene: Phaser.Scene;
    cw: number;
    ch: number;
    graphics: Phaser.GameObjects.Graphics | undefined;

    constructor(config: IAlignGridConfig) {
        if (!config.scene) {
            console.log("missing scene!");
            return;
        }
        if (!config.rows) {
            config.rows = 3;
        }
        if (!config.cols) {
            config.cols = 3;
        }
        this.h = config.height || toNum(config.scene.game.config.height);
        this.w = config.width || toNum(config.scene.game.config.width);
        this.rows = config.rows;
        this.cols = config.cols;        
        this.scene = config.scene;
        //cw cell width is the scene width divided by the number of columns
        this.cw = this.w / this.cols;
        //ch cell height is the scene height divided the number of rows
        this.ch = this.h / this.rows;
    } 
    
        // //promote to a class variable
        // this.par = par;
        

    //place an object in relation to the grid
    placeAt(xx: number, yy: number, obj: any) {
        //calculate the center of the cell
        //by adding half of the height and width
        //to the x and y of the coordinates
        var x2 = this.cw * xx + this.cw / 2;
        var y2 = this.ch * yy + this.ch / 2;
        obj.x = x2;
        obj.y = y2;
    }

    getPosByIndex(index: number) {
        var y = Math.floor(index / this.cols);
        var x = index - (y * this.cols);
        return {
            x: x * this.cw + this.cw / 2,
            y: y * this.ch + this.ch / 2
        };
    }

    placeAtIndex(index: number, obj: any) {
        var yy = Math.floor(index / this.cols);
        var xx = index - (yy * this.cols);
        this.placeAt(xx, yy, obj);
    }
    // //mostly for planning and debugging this will
    // //create a visual representation of the grid
    // show() {
    //     // game
    //     this..graphics = this.scene.add.graphics();
    //     this.graphics.lineStyle(4, 0xff0000, 1);
    //     //
    //     //
    //     for (var i = 0; i < this.par.width; i += this.cw) {
    //         this.graphics.moveTo(i, 0);
    //         this.graphics.lineTo(i, this.par.height);
    //     }
    //     for (var i = 0; i < this.par.height; i += this.ch) {
    //         this.graphics.moveTo(0, i);
    //         this.graphics.lineTo(this.par.width, i);
    //     }
    // }
    // mostly for planning and debugging this will
    // create a visual representation of the grid
    show(a = 1) {
        this.graphics = this.scene.add.graphics();
        this.graphics.lineStyle(4, 0xff0000, a);
        //
        //
        //this.graphics.beginPath();
        for (var i = 0; i < this.w; i += this.cw) {
            this.graphics.moveTo(i, 0);
            this.graphics.lineTo(i, this.h);
        }
        for (var i = 0; i < this.h; i += this.ch) {
            this.graphics.moveTo(0, i);
            this.graphics.lineTo(this.w, i);
        }
        this.graphics.strokePath();
    }
    showNumbers(a = 1) {
        this.show(a);
        var n = 0;
        for (var i = 0; i < this.rows; i++) {
            for (var j = 0; j < this.cols; j++) {
                var numText = this.scene.add.text(0, 0, n.toString(), {
                    color: 'red'
                });
                numText.setOrigin(0.5, 0.5);
                this.placeAt(j, i, numText);
                n++;
            }
        }
    }
}