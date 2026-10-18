import { InjectionToken } from '@angular/core';

/** base URL of the Causeloom backend. The dev server and nginx proxy /api to Spring Boot. */
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL');
