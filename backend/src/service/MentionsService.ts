import z from "zod";
import IOpenAiAdapter from "./IOpenAiAdapter";
import ITrainerRepository from "../domain/entities/heroAggregate/ITrainerRepository";
import IUserRepository from "../domain/entities/userAggregate/IUserRepository";
import IInstagramMessageAdapter from "./IInstagramMessageAdapter";

// sample tester message
/*{"object":"instagram","entry":[{"time":1740157200749,"id":"17841472110558136","messaging":[{"sender":{"id":"489760440872775"},"recipient":{"id":"17841472110558136"},"timestamp":1740157200421,"message":{"mid":"aWdfZAG1faXRlbToxOklHTWVzc2FnZAUlEOjE3ODQxNDcyMTEwNTU4MTM2OjM0MDI4MjM2Njg0MTcxMDMwMTI0NDI3NjAyNTg0MzYzNjQ5MDY2MjozMjEwMDIzNDUyNDE5MjQ5ODg1NjAzOTM5MjY5NzM4NDk2MAZDZD","text":"Omg"}}]}]}*/

// sample tester image message
/*{"object":"instagram","entry":[{"time":1740157287033,"id":"17841472110558136","messaging":[{"sender":{"id":"489760440872775"},"recipient":{"id":"17841472110558136"},"timestamp":1740157286706,"message":{"mid":"aWdfZAG1faXRlbToxOklHTWVzc2FnZAUlEOjE3ODQxNDcyMTEwNTU4MTM2OjM0MDI4MjM2Njg0MTcxMDMwMTI0NDI3NjAyNTg0MzYzNjQ5MDY2MjozMjEwMDIzNjExNTg4MzI3NzM3OTI0MTg2MTg1NjYyNDY0MAZDZD","attachments":[{"type":"image","payload":{"url":"https://lookaside.fbsbx.com/ig_messaging_cdn/?asset_id=1151970572602487&signature=AYddBE7nIoOZgwxUzmOcNvQdjj-29xgWns_TQqxig-qfUXUUXjn3FsBFrPGA9sDrgJPpxqhyESOuWSx8YDWN_2nDrVU1--ByBoPnPidHowPGtlqR_W5011Zjt0RbEvRwHJ4h8vDuIIB_qDVf9u3F63x5NtubhGLnBoXy2OhMJLXX_xALKGdIbrtjowK8u253rz5w_vqLJ6VR4d5jEMNjtzMvLNRvIFE"}}]}}]}]}*/

// sample message 3rd user
/*{"object":"instagram","entry":[{"time":1740157485698,"id":"17841472110558136","messaging":[{"sender":{"id":"1471737950450354"},"recipient":{"id":"17841472110558136"},"timestamp":1740157484343,"message":{"mid":"aWdfZAG1faXRlbToxOklHTWVzc2FnZAUlEOjE3ODQxNDcyMTEwNTU4MTM2OjM0MDI4MjM2Njg0MTcxMDMwMTI0NDI1OTc3MzQ2MzgxMTk3ODMwMDozMjEwMDIzOTc2MTYzMTUxNTQwMjQ4NDg3ODUzMzQ1OTk2OAZDZD","text":"Bla"}}]}]}*/

// mention sample:
/*{"entry":[{"id":"0","time":1740156774,"changes":[{"field":"mentions","value":{"media_id":"17887498072083520","comment_id":"17887498072083520"}}]}],"object":"instagram"}*/

// mentions ain't working => use forwarded publications!
/*
{
  "object":"instagram",
  "entry":[
    {
      "time":1740492173971,
      "id":"17841472110558136",
      "messaging":[
        {
          "sender":{"id":"489760440872775"},
          "recipient":{"id":"17841472110558136"},
          "timestamp":1740492172673,
          "message":{
            "mid":"aWdfZAG1faXRlbToxOklHTWVzc2FnZAUlEOjE3ODQxNDcyMTEwNTU4MTM2OjM0MDI4MjM2Njg0MTcxMDMwMTI0NDI3NjAyNTg0MzYzNjQ5MDY2MjozMjEwNjQxMzY3MTYwMjkxMzc0MzYxNzk5NjM1MTM0MDU0NAZDZD",
            "attachments":[
              {
                "type":"share",
                "payload":{
                  "url":"https://lookaside.fbsbx.com/ig_messaging_cdn/?asset_id=18145475416367289&signature=AYc5D6qrK74sOCiuPebloa13t3f4_KuOLtSmMILuQtzaAAsS1__4zT-1fdevlC4pFSODRpzokMQwcpftAyTY7SlFE2z4wPPJJwF1UZfzsl55MZpgvwr7I-QbpcHShTLwQL4rnftQdOm9yRJr3N3Cs5Jp1qqv7efw0MJ151RtJcwsgC9ikHEEHAnJakawLF6oaXlWBJYHk36V1YIbQm6jqDzTCGwycFUy"
                }
              }
            ]
          }
        }
      ]
    }
  ]
}*/

// tag not working:
export const mentionData = z.object({
  object: z.literal("instagram"),
  entry: z.array(z.object({
    time: z.number(),
    id: z.string(),
    messaging: z.optional(z.array(z.object({
      sender: z.object({ id: z.string() }),
      recipient: z.object({ id: z.string() }),
      timestamp: z.number(),
      message: z.object({
        mid: z.string(),
        text: z.ostring(),
        attachments: z.optional(z.array(z.object({
          type: z.string(),
          payload: z.object({
            url: z.string()
          })
        })))
      })
    }))),
    changes: z.optional(z.object({
      field: z.literal("mentions"),
      value: z.object({
        media_id: z.string(),
        comment_id: z.string()
      })
    }).array())
  }))
});

export type MentionData = typeof mentionData._type;

export default class MentionsService {
  constructor(
    private readonly trainerRepo: ITrainerRepository,
    private readonly userRepo: IUserRepository,
    private readonly analyzer: IOpenAiAdapter,
    private readonly instagram: IInstagramMessageAdapter,
    private readonly query: string
  ) {}

  async handle(mention: MentionData) {
    for (const entry of mention.entry) {
      if ((entry.messaging?.length || 0) > 0) {
        // Message Entry
        const shares = (entry.messaging || []).filter((m) => m.message.attachments?.length === 1 && m.message.attachments[0].type === "share");
        for (const share of shares) {
          if (share.recipient.id !== this.instagram.userId()) {
            console.error(`Got a message webhook for other user: ${share.recipient.id}`);
          } else {
            const userId = share.sender.id;
            const timestamp = share.timestamp;
            const mediaUrl = share.message.attachments![0].payload.url;
            const media = await fetch(mediaUrl);
            const mediaStream = await media.arrayBuffer();
            const mediaBuffer = Buffer.from(mediaStream);
            const user = await this.userRepo.findByInstagramId(userId);
            if (!user) {
              throw new Error(`Could not find instagram user ${userId}`);
            }
            const trainer = await this.trainerRepo.getOrCreateTrainer(user.id);
            if (!trainer.canTrain()) {
              await this.instagram.respond(userId, `[Heroes of Penta XP Bot]: You are not ready to train your hero(es) again! Please wait at least 6 hours between trainings!`);
            } else if (trainer.traineeCount < 1) {
              await this.instagram.respond(userId, `[Heroes of Penta XP Bot]: You have no hero(es) selected for training!`);
            } else if (trainer.readyTraineeCount < 1) {
              await this.instagram.respond(userId, `[Heroes of Penta XP Bot]: All your heroes are still tired from training, did not train!`);
            } else {
              const disposition = await this.analyzer.analyzeImage(this.query, mediaBuffer);
              const xp = trainer.train(timestamp, new Map(Object.entries(disposition)));
              await this.trainerRepo.save(trainer);
              await this.instagram.respond(userId, `[Heroes of Penta XP Bot]: ${xp} XP have been awarded!`);
            }
          }
        }
      }
    }

    // As the service name suggests, I wanted to do this with mentions - unfortunately, impossible => use message sharing instead!
    // const candidateEntries = mention.entry.filter((e) => (e.changes || []).find((c) => c.field === "mentions"));

    // for (const entry of candidateEntries) {
    //   const changes = entry.changes!.filter((c) => c.field === "mentions").map((c) => c.value);
    //   for (const change of changes) {
    //     const m = new Mention(change.media_id, change.comment_id, entry.time);
    //     await this.repo.push(m);
    //   }
    // }
  } 
}