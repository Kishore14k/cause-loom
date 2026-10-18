export interface Person {
  name: string;
  role: string;
  onLeave: boolean;
}

export interface OnCallResult {
  service: string;
  team: string | null;
  onCall: Person | null;
  responder: Person | null;
  escalated: boolean;
  message: string;
}
