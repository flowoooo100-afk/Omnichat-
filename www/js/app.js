const pages = document.querySelectorAll('.page');
const navItems = document.querySelectorAll('.nav-item');

const input = document.getElementById('messageInput');
const messages = document.getElementById('messages');
const welcome = document.getElementById('welcome');

function showPage(pageId) {
  pages.forEach(p => p.classList.toggle('active', p.id === pageId));
  navItems.forEach(b => b.classList.toggle('selected', b.dataset.page === pageId));
}

navItems.forEach(button => {
  button.addEventListener('click', () => showPage(button.dataset.page));
});

function addMessage(text, type) {
  const div = document.createElement('div');
  div.className = 'message ' + type;
  div.textContent = text;
  messages.appendChild(div);
  messages.scrollTop = messages.scrollHeight;
}

function sendMessage() {
  const text = input.value.trim();
  if (!text) return;

  welcome.style.display = 'none';
  addMessage(text, 'user');

  input.value = '';
  input.style.height = 'auto';

  const notice = document.createElement('div');
  notice.className = 'message assistant';
  notice.textContent = 'Your message is ready. Connect an AI provider in Models to start receiving real AI responses.';
  messages.appendChild(notice);
  messages.scrollTop = messages.scrollHeight;
}

document.getElementById('sendBtn').addEventListener('click', sendMessage);

input.addEventListener('keydown', event => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    sendMessage();
  }
});

input.addEventListener('input', () => {
  input.style.height = 'auto';
  input.style.height = Math.min(input.scrollHeight, 120) + 'px';
});

document.querySelectorAll('.suggestion').forEach(button => {
  button.addEventListener('click', () => {
    input.value = button.dataset.prompt;
    input.focus();
  });
});

function newChat() {
  messages.innerHTML = '';
  welcome.style.display = 'flex';
  input.value = '';
  showPage('chatPage');
}

document.getElementById('newChat').addEventListener('click', newChat);
