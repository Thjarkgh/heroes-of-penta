import { promisify } from "util";
import { randomBytes, scrypt } from "crypto";
const scryptWithIterations = (pt: string, salt: string, keylen: number, iterations: number, cb: (err: Error | null, derivedKey: Buffer<ArrayBufferLike>) => void) =>
  scrypt(pt, salt, keylen, { N: iterations }, cb);
const scryptAsync = promisify(scryptWithIterations);

export interface Secret {
  salt: string,
  secret: string
}

export const generateSecret = async (msg: string): Promise<Secret> => {
  const globalSalt = process.env.SALT;
  if (!globalSalt) throw new Error(`no global salt!`);

  const salt = randomBytes(16).toString("hex");
  const effectiveSalt = await scryptAsync(globalSalt, salt, 32, 8192);
  const hash = await scryptAsync(msg, effectiveSalt.toString("hex"), 32, 8192);
  return {
    salt,
    secret: hash.toString("hex")
  };
};

export const verifySecret = async (msg: string, secret: Secret): Promise<boolean> => {
  const globalSalt = process.env.SALT;
  if (!globalSalt) throw new Error(`no global salt!`);

  const effectiveSalt = await scryptAsync(globalSalt, secret.salt, 32, 8192);
  const hash = await scryptAsync(msg, effectiveSalt.toString("hex"), 32, 8192);
  return hash.toString("hex") === secret.secret;
};