export const appState = {
    token: localStorage.getItem('photon-admin-token') || '',
    account: null,
    tables: [],
    activeTable: null,
    servers: [],
    config: null,
    page: window.location.hash.replace('#', '') || 'overview',
};

export const configFields = [
    { key: 'bot_activity', label: 'Bot activity', type: 'text' },
    { key: 'discord_bot_token', label: 'Discord bot token', type: 'password' },
    { key: 'discord_bot_id', label: 'Discord bot ID', type: 'text' },
    { key: 'official_discord_server_id', label: 'Official Discord server ID', type: 'text' },
    { key: 'network_console_channel_id', label: 'Console channel ID', type: 'text' },
    { key: 'server_creator_role_id', label: 'Server creator role ID', type: 'text' },
    { key: 'webserver_port', label: 'Webserver port', type: 'number' },
    { key: 'license_product_id', label: 'License product ID', type: 'text' },
    { key: 'tebex_webhook_secret', label: 'Tebex webhook secret', type: 'password' },
    { key: 'license_default_duration_days', label: 'Default license duration (days)', type: 'number' },
    { key: 'api_version', label: 'API version', type: 'text' },
    { key: 'mod_version', label: 'Mod version', type: 'text' },
    { key: 'launcher_version', label: 'Launcher version', type: 'text' },
    { key: 'twitter_url', label: 'Twitter URL', type: 'url' },
    { key: 'twitch_url', label: 'Twitch URL', type: 'url' },
    { key: 'youtube_url', label: 'YouTube URL', type: 'url' },
    { key: 'discord_url', label: 'Discord URL', type: 'url' },
    { key: 'website_url', label: 'Website URL', type: 'url' },
];

export const pageDefinitions = [
    { key: 'overview', label: 'Overview' },
    { key: 'status', label: 'Status' },
    { key: 'config', label: 'Config' },
    { key: 'tables', label: 'Tables' },
    { key: 'operations', label: 'Operations' },
];