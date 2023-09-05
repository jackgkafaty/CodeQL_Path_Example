/*
 * Copyright (c) 2014-2023 Bjoern Kimminich & the OWASP Juice Shop contributors.
 * SPDX-License-Identifier: MIT
 */

import utils = require('../lib/utils')
import challengeUtils = require('../lib/challengeUtils')
import { Request, Response, NextFunction } from 'express'
import { Review } from 'data/types'

const challenges = require('../data/datacache').challenges
const security = require('../lib/insecurity')
const db = require('../data/mongodb')

// Blocking sleep function as in native MongoDB
// @ts-expect-error
global.sleep = (time: number) => {
  // Ensure that users don't accidentally dos their servers for too long
  if (time > 2000) {
    time = 2000
  }
  const stop = new Date().getTime()
  while (new Date().getTime() < stop + time) {
    ;
  }
}

