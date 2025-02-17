import { getBaseDir } from "../../src/helper/getBaseDir";
import { describe, it, expect } from "vitest";
import path from "path";

describe("getBaseDir", () => {
  it("should find root", () => {
    const feFolder = getBaseDir(__dirname);

    expect(feFolder).toEqual(path.join(__dirname, "..", "..", "..", "www"));
  });

  it("should not find root from outside folder", () => {
    expect(() => getBaseDir("/")).toThrow();
  });
});