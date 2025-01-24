import { describe, it, expect } from "vitest";
import { mockDeep } from "vitest-mock-extended";
import { AlignGrid } from "../../../src/game/components/alignGrid";
import Phaser from "phaser";

describe("AlignGrid", () => {
    it("should", () => {
        const mockScene = mockDeep<Phaser.Scene>();
        // @ts-ignore
        mockScene.game.config.width = 1000;
        // @ts-ignore
        mockScene.game.config.height = 500;
        const config = {
            cols: 10,
            rows: 30,
            scene: mockScene
        };

        const ag = new AlignGrid(config);

        expect(ag.cw).toEqual(mockScene.game.config.width / config.cols);
        expect(ag.ch).toEqual(mockScene.game.config.height / config.rows);
        expect(ag.cols).toEqual(config.cols);
        expect(ag.rows).toEqual(config.rows);
    })
})