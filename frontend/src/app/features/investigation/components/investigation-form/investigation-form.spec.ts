import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';

import { InvestigationForm } from './investigation-form';

describe('InvestigationForm', () => {
  it('trims submitted input and ignores empty or disabled submissions', () => {
    const fixture = TestBed.createComponent(InvestigationForm);
    const form = fixture.componentInstance;
    const listener = vi.fn();
    form.submitted.subscribe(listener);

    form.message = '   ';
    form.submit();
    form.message = '  Find the cause  ';
    form.submit();
    fixture.componentRef.setInput('loading', true);
    form.submit();

    expect(listener).toHaveBeenCalledExactlyOnceWith('Find the cause');
  });
});
