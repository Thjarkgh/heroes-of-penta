import { AlignGrid } from "./alignGrid";
import { toNum } from "./common";

export interface IFormUtilConfig {
  scene: Phaser.Scene,
  rows: number,
  cols: number
}

export class FormUtil {
  scene: Phaser.Scene;
  gameWidth: number;
  gameHeight: number;
  alignGrid: AlignGrid
  constructor(config: IFormUtilConfig) {
    //super();
    this.scene = config.scene;
    //get the game height and width
    this.gameWidth = toNum(this.scene.game.config.width);
    this.gameHeight = toNum(this.scene.game.config.height);
    this.alignGrid = new AlignGrid({
      scene: this.scene,
      rows: config.rows,
      cols: config.cols
    });
  }
  showNumbers() {
      this.alignGrid.showNumbers();
  }
  scaleToGameW(elName: string, per: number) {
    var el = document.getElementById(elName);
    var w = this.gameWidth * per;
    if (!!el) el.style.width = w + "px";
  }
  scaleToGameH(elName: string, per: number) {
    var el = document.getElementById(elName);
    var h = this.gameHeight * per;
    if (!!el) el.style.height = h + "px";
  }
  placeElementAt(index: number, elName: string, centerX = true, centerY = false) {
    //get the position from the grid
    var pos = this.alignGrid.getPosByIndex(index);
    //extract to local vars
    var x = pos.x;
    var y = pos.y;
    //get the element
    var el = document.getElementById(elName);
    //set the position to absolute
    if (!!el) {
      el.style.position = "absolute";
      //get the width of the element
      //convert to a number
      const w = toNum(el.style.width);
      //
      //
      //center horizontal in square if needed
      if (centerX == true) {
        x -= w / 2;
      }
      //
      //get the height
      //convert to a number
      const h = toNum(el.style.height);
      //
      //center verticaly in square if needed
      //
      if (centerY == true) {
        y -= h / 2;
      }
      //set the positions
      el.style.top = y + "px";
      el.style.left = x + "px";
    }
  }

  //add a change callback
  addChangeCallback(elName: string, fun: ((this: GlobalEventHandlers, ev: Event) => any) | null, scope: GlobalEventHandlers | null = null) {
    var el = document.getElementById(elName);
    if (!!el) {
      if (scope == null) {
        el.onchange = fun;
      } else if (fun != null) {
        el.onchange = fun.bind(scope);
      }
    }
  }

  getTextAreaValue(elName: string) {
    var el = document.getElementById(elName);
    return el ? (el as any).value : null;
  }
  getTextValue(elName: string) {
    var el = document.getElementById(elName);
    return el ? el.innerText : null;
  }
  hideElement(elName: string) {
    var el = document.getElementById(elName);
    if (!!el) el.style.display = "none";
  }
  showElement(elName: string) {
    var el = document.getElementById(elName);
    if (!!el) el.style.display = "block";
  }
}