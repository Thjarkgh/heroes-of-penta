import httpError from 'http-errors';
import XHubVerifier from './xHubVerifier';
import { Request, Response, NextFunction } from "express";

const header = 'X-Hub-Signature-256';

export default function verifyXHubSignature(secret: string, require: boolean = true) {
  const x = new XHubVerifier(secret);

  return (req: Request, res: Response, next: NextFunction) => {
    const rawBody = (req as Request & { rawBody: string }).rawBody;
    if (!rawBody) {
      return next(httpError(500, 'Missing req.rawBody, see the x-hub-signature readme'));
    }

    const signature = req.header(header);

    if (require && !signature) {
      return next(httpError(400, `Missing ${header} header`));
    }

    if (signature) {
      //const body = Buffer.from(rawBody)

      if (!x.verify(signature, rawBody)) {
        return next(httpError(400, `Invalid ${header}: "${rawBody}"`));
      }
    }

    next();
  }
}