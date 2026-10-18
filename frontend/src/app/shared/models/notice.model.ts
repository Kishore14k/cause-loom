export interface Notice {
  kind: 'success' | 'error' | 'info';
  message: string;
}
