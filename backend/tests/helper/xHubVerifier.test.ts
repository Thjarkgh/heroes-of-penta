import XHubVerifier from "../../src/helper/xHubVerifier";
import { describe, expect, it } from "vitest";

describe("xHubVerifier", () => {
  describe('constructor', () => {
    it('should throw when secret is missing', () => {
      expect(() => new XHubVerifier('')).toThrow();
    });
  });

  describe('sign', () => {
    it('should sign with valid arguments', () => {
      const expected = 'sha256=016fe3d7fe58dd03a6ba6339ab3b3b5d5e3edb5972e7752226106915fb0a6993';
      const secret = 'TEST_SECRET';
      const body = '{"test":"TEST"}';
      const x = new XHubVerifier(secret);

      const signature = x.sign(body);

      expect(signature).toEqual(expected);
    });

    it('should sign with valid UTF-8 arguments', () => {
      const expected = 'sha256=fce263f10fb5eadf060030c62b31b1e2996a17eb6dbadb09aeaa60f23a317ce3';
      const secret = 'TEST_SECRET';
      const body = '{"test":"あいうえお"}';
      const x = new XHubVerifier(secret);

      const signature = x.sign(body);

      expect(signature).toEqual(expected);
    });
  });

  describe('verify', () => {
    it('should return true when valid', () => {
      const expected = 'sha256=016fe3d7fe58dd03a6ba6339ab3b3b5d5e3edb5972e7752226106915fb0a6993';
      const secret = 'TEST_SECRET';
      const body = '{"test":"TEST"}';
      const x = new XHubVerifier(secret);

      const isValid = x.verify(expected, body);
      
      expect(isValid).toEqual(true);
    });

    it('should return true when valid (UTF-8)', () => {
      const expected = 'sha256=fce263f10fb5eadf060030c62b31b1e2996a17eb6dbadb09aeaa60f23a317ce3';
      const secret = 'TEST_SECRET';
      const body = '{"test":"あいうえお"}';
      const x = new XHubVerifier(secret);

      const isValid = x.verify(expected, body);
      
      expect(isValid).toEqual(true);
    });

    it('should return false when signature is empty', () => {
      const secret = 'TEST_SECRET';
      const body = '{"test":"TEST"}';
      const x = new XHubVerifier(secret);
      
      const isValid = x.verify('', body);
      
      expect(isValid).toEqual(false);
    })

    it('should return false when signature is invalid', () => {
      const secret = 'TEST_SECRET';
      const body = '{"test":"TEST"}';
      const x = new XHubVerifier(secret);
      
      const isValid = x.verify('sha256=fce263f10fb5eadf060030c62b31b1e2996a17eb6dbadb09aeaa60f23a317ce3', body);
      
      expect(isValid).toEqual(false);
    })

    it('should return false when signature algorithm does not match', () => {
      const secret = 'TEST_SECRET';
      const body = '{"test":"TEST"}';
      const x = new XHubVerifier(secret);
      
      const isValid = x.verify('sha1=016fe3d7fe58dd03a6ba6339ab3b3b5d5e3edb5972e7752226106915fb0a6993', body);
      
      expect(isValid).toEqual(false);
    });
  });
});