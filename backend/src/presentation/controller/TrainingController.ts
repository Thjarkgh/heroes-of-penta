import { Express, Request, Response, NextFunction } from 'express';
import multer from "multer";
import TrainingService from '../../service/TrainingService';
import User from '../../domain/entities/userAggregate/User';

const storage = multer.memoryStorage();
const upload = multer({ storage: storage, limits: { fileSize: 5 * 1024 * 1024 } });

export default class TrainingController {
  constructor(
    private readonly service: TrainingService
  ) {
  }

  public setup(app: Express) {
    // Callback route from Instagram: GET /connect/instagram
    // app.get('/selfie', this.handleTrainingRequest.bind(this));
    app.post('/training/selfie', upload.single("selfie"), this.handleTrainingSubmission.bind(this))
  }    /*@Multipart
  @POST("training/selfie")
  fun uploadSelfie(
    @Part selfie: MultipartBody.Part,
    @Part("selectedHeroIds") selectedHeroIds: RequestBody
  ): Call<BasicResponse>*/

  private async handleTrainingSubmission(req: Request, res: Response, next: NextFunction) {
    try {
      if (!req.user) {
        res.status(401).send("Not logged in");
        return;
      }
      const data = req.file?.buffer;
      if (!data) {
        res.status(400).send("no selfie");
        return;
      }
      // TODO: Typesafety!
      const selectedHeroIds = req.body.selectedHeroIds.toString().split(',').map((p: string) => Number.parseInt(p));
      const result = await this.service.train((req.user as User).id, selectedHeroIds, data);
      console.log(result);
      res.json(result);

    } catch (error: any) {
      console.error('Error exchanging code for token:', error.message);
      res.status(500).send('Error exchanging code for token');
    }
  }
}
