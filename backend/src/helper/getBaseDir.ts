import { existsSync } from 'fs';
import path from 'path';

export const getBaseDir = (dir: string) => {
  if (existsSync(path.resolve(dir, 'build')) && existsSync(path.resolve(dir, 'www'))) { return path.resolve(dir, 'www'); }
  else if (existsSync(path.resolve(dir, '..', 'build')) && existsSync(path.resolve(dir, '..', 'www'))) { return path.resolve(dir, '..', 'www'); }
  else if (existsSync(path.resolve(dir, '..', '..', 'build')) && existsSync(path.resolve(dir, '..', '..', 'www'))) { return path.resolve(dir, '..', '..', 'www'); }
  else if (existsSync(path.resolve(dir, '..', '..', '..', 'build')) && existsSync(path.resolve(dir, '..', '..', '..', 'www'))) { return path.resolve(dir, '..', '..', '..', 'www'); }
  else {
    throw new Error(`Cannot find folder dist and build for Forntend to serve ${dir} and two folders up`);
  }
}