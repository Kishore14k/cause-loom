import { HttpErrorResponse } from '@angular/common/http';
import { TimeoutError } from 'rxjs';

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof TimeoutError) {
    return 'The request timed out. The backend may still finish it, so refresh before retrying.';
  }
  if (error instanceof HttpErrorResponse) {
    if ([0, 502, 503, 504].includes(error.status)) {
      return 'Could not reach the backend. Check that Spring Boot and FalkorDB are running. What you see may be out of date.';
    }
    if (error.error && typeof error.error === 'object' && typeof error.error.message === 'string') {
      return error.error.message;
    }
    return `${fallback} (HTTP ${error.status}). Check the backend logs before retrying.`;
  }
  return fallback;
}
