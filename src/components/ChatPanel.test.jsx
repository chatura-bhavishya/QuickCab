import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import ChatPanel from './ChatPanel';

const messages = [
  { id: 1, sender: 'USER', message: 'Where are you?' },
  { id: 2, sender: 'DRIVER', message: 'Two minutes away' }
];

describe('ChatPanel', () => {
  it('marks the viewer\'s own messages', () => {
    render(<ChatPanel title="Chat" messages={messages} ownSender="USER" onSend={vi.fn()} />);

    expect(screen.getByText('Where are you?')).toHaveClass('me');
    expect(screen.getByText('Two minutes away')).toHaveClass('them');
  });

  it('shows an empty state', () => {
    render(<ChatPanel title="Chat" messages={[]} ownSender="USER" onSend={vi.fn()} />);
    expect(screen.getByText('No messages yet.')).toBeInTheDocument();
  });

  it('sends the draft and clears the input', async () => {
    const onSend = vi.fn().mockResolvedValue();
    render(<ChatPanel title="Chat" messages={[]} ownSender="USER" onSend={onSend} />);

    const input = screen.getByLabelText('Message');
    await userEvent.type(input, 'Hello');
    await userEvent.click(screen.getByRole('button', { name: 'Send' }));

    expect(onSend).toHaveBeenCalledWith('Hello');
    expect(input).toHaveValue('');
  });

  it('keeps the draft and shows an error when sending fails', async () => {
    const onSend = vi.fn().mockRejectedValue({ response: { data: { error: 'Chat is closed' } } });
    render(<ChatPanel title="Chat" messages={[]} ownSender="DRIVER" onSend={onSend} />);

    await userEvent.type(screen.getByLabelText('Message'), 'Hi');
    await userEvent.click(screen.getByRole('button', { name: 'Send' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Chat is closed');
    expect(screen.getByLabelText('Message')).toHaveValue('Hi');
  });

  it('ignores blank messages', async () => {
    const onSend = vi.fn();
    render(<ChatPanel title="Chat" messages={[]} ownSender="USER" onSend={onSend} />);

    await userEvent.type(screen.getByLabelText('Message'), '   ');
    await userEvent.click(screen.getByRole('button', { name: 'Send' }));

    expect(onSend).not.toHaveBeenCalled();
  });
});
