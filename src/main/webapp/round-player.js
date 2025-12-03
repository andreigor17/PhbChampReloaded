// ========================================
// CS2 Round Player - JavaScript
// ========================================

class RoundPlayer {
    constructor() {
        this.canvas = document.getElementById('minimapCanvas');
        this.ctx = this.canvas.getContext('2d');
        
        // Estado do player
        this.isPlaying = false;
        this.currentTick = 0;
        this.speed = 1;
        this.animationFrameId = null;
        this.lastFrameTime = 0;
        
        // Dados do round
        this.roundData = null;
        this.ticks = [];
        this.events = [];
        this.players = {};
        
        // Configurações do mapa
        this.mapConfig = {
            'de_dust2': { minX: -2476, maxX: 3239, minY: -2032, maxY: 3563, size: 1024 },
            'de_mirage': { minX: -3230, maxX: 1713, minY: -3401, maxY: 1825, size: 1024 },
            'de_inferno': { minX: -2087, maxX: 3870, minY: -784, maxY: 3521, size: 1024 },
            'de_nuke': { minX: -3453, maxX: 2887, minY: -3565, maxY: 3202, size: 1024 },
            'de_vertigo': { minX: -3168, maxX: 1840, minY: -2806, maxY: 2026, size: 1024 },
            'de_ancient': { minX: -2953, maxX: 2404, minY: -3521, maxY: 2143, size: 1024 },
            'de_anubis': { minX: -2796, maxX: 2032, minY: -3320, maxY: 1864, size: 1024 },
            'de_cache': { minX: -2000, maxX: 3250, minY: -2000, maxY: 3000, size: 1024 },
            'default': { minX: -4000, maxX: 4000, minY: -4000, maxY: 4000, size: 1024 }
        };
        
        this.currentMap = 'default';
        
        this.init();
    }
    
    init() {
        this.setupEventListeners();
        this.loadRoundData();
    }
    
    setupEventListeners() {
        // Play/Pause
        document.getElementById('btnPlayPause').addEventListener('click', () => this.togglePlayPause());
        
        // Stop
        document.getElementById('btnStop').addEventListener('click', () => this.stop());
        
        // Restart
        document.getElementById('btnRestart').addEventListener('click', () => this.restart());
        
        // Timeline slider
        const slider = document.getElementById('timelineSlider');
        slider.addEventListener('input', (e) => this.seekToTick(parseInt(e.target.value)));
        
        // Speed buttons
        document.querySelectorAll('.speed-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const speed = parseFloat(e.target.dataset.speed);
                this.setSpeed(speed);
                
                // Update active state
                document.querySelectorAll('.speed-btn').forEach(b => b.classList.remove('active'));
                e.target.classList.add('active');
            });
        });
        
        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => {
            if (e.code === 'Space') {
                e.preventDefault();
                this.togglePlayPause();
            } else if (e.code === 'ArrowLeft') {
                e.preventDefault();
                this.seekToTick(Math.max(0, this.currentTick - 10));
            } else if (e.code === 'ArrowRight') {
                e.preventDefault();
                this.seekToTick(Math.min(this.ticks.length - 1, this.currentTick + 10));
            }
        });
    }
    
    async loadRoundData() {
        try {
            // Get anexoId and roundNumber from URL
            const params = new URLSearchParams(window.location.search);
            const anexoId = params.get('anexoId');
            const roundNumber = params.get('round');
            
            if (!anexoId) {
                throw new Error('ID do anexo não especificado');
            }
            
            // Load demo analysis
            const apiUrl = this.getApiBaseUrl();
            const response = await fetch(`${apiUrl}/api/demo/analyze/${anexoId}`);
            
            if (!response.ok) {
                throw new Error(`Erro ao carregar dados: ${response.status}`);
            }
            
            const data = await response.json();
            
            // Extract round data
            if (roundNumber !== null) {
                const round = data.rounds.find(r => r.roundNumber == roundNumber);
                if (round) {
                    this.roundData = round;
                }
            } else {
                // Use first round if not specified
                this.roundData = data.rounds[0];
            }
            
            if (!this.roundData) {
                throw new Error('Round não encontrado');
            }
            
            // Set map name
            this.currentMap = data.header.mapName || 'default';
            if (!this.mapConfig[this.currentMap]) {
                this.currentMap = 'default';
            }
            
            // Check if we have real tick data
            if (this.roundData.ticksData && this.roundData.ticksData.ticks) {
                console.log('Using REAL tick data from demo');
                this.loadRealTickData();
            } else {
                console.log('No tick data available, using simulated data');
                this.generateTicksFromRound();
            }
            
            // Hide loading, show content
            document.getElementById('loading').style.display = 'none';
            document.getElementById('mainContent').style.display = 'grid';
            document.getElementById('controls').style.display = 'block';
            
            // Update UI
            this.updateRoundInfo();
            this.updateTimeline();
            this.drawFrame();
            
        } catch (error) {
            console.error('Error loading round data:', error);
            document.getElementById('loading').style.display = 'none';
            const errorDiv = document.getElementById('error');
            errorDiv.textContent = `❌ Erro: ${error.message}`;
            errorDiv.style.display = 'block';
        }
    }
    
    loadRealTickData() {
        console.log('Loading REAL tick data from Python parser');
        
        const ticksData = this.roundData.ticksData;
        
        if (!ticksData || !ticksData.ticks) {
            console.error('No ticks data in round');
            this.generateTicksFromRound();
            return;
        }
        
        console.log(`Loading ${ticksData.ticks.length} ticks from tick ${ticksData.startTick} to ${ticksData.endTick}`);
        
        // Process real ticks
        this.ticks = ticksData.ticks.map(tickData => {
            const tick = {
                tickNumber: tickData.tick,
                players: {}
            };
            
            tickData.players.forEach(player => {
                tick.players[player.name] = {
                    x: player.x,
                    y: player.y,
                    z: player.z,
                    team: player.team,
                    isAlive: player.isAlive,
                    health: player.health,
                    armor: player.armor || 0,
                    weapon: player.weapon || ''
                };
                
                // Initialize player if not exists
                if (!this.players[player.name]) {
                    this.players[player.name] = {
                        name: player.name,
                        team: player.team,
                        isAlive: player.isAlive,
                        health: player.health
                    };
                }
            });
            
            return tick;
        });
        
        // Create events from kills with estimated tick positions
        this.events = this.roundData.kills.map((kill, idx) => {
            // Try to find the tick where this kill happened (based on position if available)
            // For now, distribute evenly across the round
            const estimatedTick = Math.floor((idx + 1) * (this.ticks.length / (this.roundData.kills.length + 1)));
            
            return {
                tick: estimatedTick,
                type: 'kill',
                killer: kill.killerName,
                victim: kill.victimName,
                weapon: kill.weapon,
                headshot: kill.headshot,
                killerTeam: kill.killerTeam,
                victimTeam: kill.victimTeam
            };
        });
        
        console.log(`Loaded ${this.ticks.length} ticks and ${this.events.length} events`);
    }
    
    generateTicksFromRound() {
        console.warn('Using SIMULATED tick data (no real positions available)');
        
        const numTicks = 600; // ~10 seconds at 64 tick rate
        this.ticks = [];
        
        // Get player names from kills
        const playerNames = new Set();
        this.roundData.kills.forEach(kill => {
            if (kill.killerName && kill.killerName !== 'World/Unknown') {
                playerNames.add(kill.killerName);
            }
            if (kill.victimName && kill.victimName !== 'Unknown') {
                playerNames.add(kill.victimName);
            }
        });
        
        // Initialize players
        let idx = 0;
        playerNames.forEach(name => {
            const isT = idx % 2 === 0; // Simplified team assignment
            this.players[name] = {
                name: name,
                team: isT ? 'TERRORIST' : 'CT',
                isAlive: true,
                health: 100,
                armor: 100
            };
            idx++;
        });
        
        // Generate ticks with simulated movement
        for (let i = 0; i < numTicks; i++) {
            const tick = {
                tickNumber: i,
                players: {}
            };
            
            Object.keys(this.players).forEach(playerName => {
                // Simulate random movement
                const baseX = Math.random() * 2000 - 1000;
                const baseY = Math.random() * 2000 - 1000;
                const offsetX = Math.sin(i / 20) * 100;
                const offsetY = Math.cos(i / 20) * 100;
                
                tick.players[playerName] = {
                    x: baseX + offsetX,
                    y: baseY + offsetY,
                    z: 0,
                    isAlive: this.players[playerName].isAlive,
                    health: this.players[playerName].health,
                    team: this.players[playerName].team
                };
            });
            
            this.ticks.push(tick);
        }
        
        // Add death events
        this.events = this.roundData.kills.map((kill, idx) => ({
            tick: Math.floor((idx + 1) * (numTicks / (this.roundData.kills.length + 1))),
            type: 'kill',
            killer: kill.killerName,
            victim: kill.victimName,
            weapon: kill.weapon,
            headshot: kill.headshot
        }));
        
        // Process events to mark players as dead
        this.events.forEach(event => {
            if (event.type === 'kill' && this.players[event.victim]) {
                this.players[event.victim].isAlive = false;
                this.players[event.victim].health = 0;
                
                // Update all ticks after this event
                for (let i = event.tick; i < this.ticks.length; i++) {
                    if (this.ticks[i].players[event.victim]) {
                        this.ticks[i].players[event.victim].isAlive = false;
                        this.ticks[i].players[event.victim].health = 0;
                    }
                }
            }
        });
        
        console.log(`Generated ${this.ticks.length} simulated ticks`);
    }
    
    getApiBaseUrl() {
        const hostname = window.location.hostname;
        if (hostname === 'localhost' || hostname === '127.0.0.1') {
            return 'http://localhost:9090';
        }
        return `${window.location.protocol}//${hostname}:9090`;
    }
    
    // ========================================
    // Playback Controls
    // ========================================
    
    togglePlayPause() {
        if (this.isPlaying) {
            this.pause();
        } else {
            this.play();
        }
    }
    
    play() {
        if (this.currentTick >= this.ticks.length - 1) {
            this.restart();
        }
        
        this.isPlaying = true;
        document.getElementById('btnPlayPause').textContent = '⏸️';
        this.lastFrameTime = performance.now();
        this.animate();
    }
    
    pause() {
        this.isPlaying = false;
        document.getElementById('btnPlayPause').textContent = '▶️';
        if (this.animationFrameId) {
            cancelAnimationFrame(this.animationFrameId);
        }
    }
    
    stop() {
        this.pause();
        this.currentTick = 0;
        this.updateTimeline();
        this.drawFrame();
    }
    
    restart() {
        this.currentTick = 0;
        this.updateTimeline();
        this.drawFrame();
    }
    
    setSpeed(speed) {
        this.speed = speed;
    }
    
    seekToTick(tick) {
        this.currentTick = Math.max(0, Math.min(this.ticks.length - 1, tick));
        this.updateTimeline();
        this.drawFrame();
    }
    
    animate() {
        if (!this.isPlaying) return;
        
        const now = performance.now();
        const deltaTime = now - this.lastFrameTime;
        
        // 64 tick = 15.625ms per tick
        // Adjust for speed multiplier
        const tickDuration = 15.625 / this.speed;
        
        if (deltaTime >= tickDuration) {
            this.lastFrameTime = now;
            this.currentTick++;
            
            if (this.currentTick >= this.ticks.length) {
                this.pause();
                this.currentTick = this.ticks.length - 1;
                return;
            }
            
            this.updateTimeline();
            this.drawFrame();
            this.checkEvents();
        }
        
        this.animationFrameId = requestAnimationFrame(() => this.animate());
    }
    
    // ========================================
    // Rendering
    // ========================================
    
    drawFrame() {
        if (!this.ticks.length || !this.ticks[this.currentTick]) return;
        
        const tick = this.ticks[this.currentTick];
        const ctx = this.ctx;
        const canvas = this.canvas;
        
        // Clear canvas
        ctx.fillStyle = '#1a202c';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        
        // Draw grid
        this.drawGrid();
        
        // Draw players
        Object.entries(tick.players).forEach(([playerName, playerData]) => {
            this.drawPlayer(playerName, playerData);
        });
        
        // Update player list
        this.updatePlayerList(tick);
    }
    
    drawGrid() {
        const ctx = this.ctx;
        const canvas = this.canvas;
        
        ctx.strokeStyle = '#2d3748';
        ctx.lineWidth = 1;
        
        // Vertical lines
        for (let x = 0; x < canvas.width; x += canvas.width / 10) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, canvas.height);
            ctx.stroke();
        }
        
        // Horizontal lines
        for (let y = 0; y < canvas.height; y += canvas.height / 10) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(canvas.width, y);
            ctx.stroke();
        }
    }
    
    drawPlayer(name, playerData) {
        const coords = this.worldToCanvas(playerData.x, playerData.y);
        const ctx = this.ctx;
        
        // Determine color
        let color = '#718096'; // Dead
        if (playerData.isAlive) {
            color = playerData.team === 'CT' ? '#3182ce' : '#e53e3e';
        }
        
        // Draw player circle
        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.arc(coords.x, coords.y, playerData.isAlive ? 14 : 10, 0, Math.PI * 2);
        ctx.fill();
        
        // Draw border
        ctx.strokeStyle = '#fff';
        ctx.lineWidth = 2;
        ctx.stroke();
        
        // Draw name
        ctx.fillStyle = '#fff';
        ctx.font = 'bold 13px sans-serif';
        ctx.textAlign = 'center';
        ctx.shadowColor = 'rgba(0, 0, 0, 0.8)';
        ctx.shadowBlur = 4;
        ctx.fillText(name, coords.x, coords.y - 22);
        ctx.shadowBlur = 0;
        
        // Draw weapon if alive
        if (playerData.isAlive && playerData.weapon) {
            ctx.fillStyle = '#4a5568';
            ctx.font = '10px sans-serif';
            ctx.fillText(playerData.weapon, coords.x, coords.y + 28);
        }
        
        // Draw health bar if alive
        if (playerData.isAlive && playerData.health > 0) {
            const barWidth = 36;
            const barHeight = 5;
            const barX = coords.x - barWidth / 2;
            const barY = coords.y + 20;
            
            // Background
            ctx.fillStyle = '#2d3748';
            ctx.fillRect(barX, barY, barWidth, barHeight);
            
            // Health
            const healthWidth = (playerData.health / 100) * barWidth;
            const healthColor = playerData.health > 50 ? '#38a169' : playerData.health > 25 ? '#ed8936' : '#e53e3e';
            ctx.fillStyle = healthColor;
            ctx.fillRect(barX, barY, healthWidth, barHeight);
            
            // Border
            ctx.strokeStyle = '#2d3748';
            ctx.lineWidth = 1;
            ctx.strokeRect(barX, barY, barWidth, barHeight);
        } else if (!playerData.isAlive) {
            // Draw X for dead players
            ctx.strokeStyle = '#fff';
            ctx.lineWidth = 3;
            const size = 8;
            ctx.beginPath();
            ctx.moveTo(coords.x - size, coords.y - size);
            ctx.lineTo(coords.x + size, coords.y + size);
            ctx.moveTo(coords.x + size, coords.y - size);
            ctx.lineTo(coords.x - size, coords.y + size);
            ctx.stroke();
        }
    }
    
    worldToCanvas(worldX, worldY) {
        const mapConfig = this.mapConfig[this.currentMap];
        const canvas = this.canvas;
        
        // Normalize coordinates
        const normalizedX = (worldX - mapConfig.minX) / (mapConfig.maxX - mapConfig.minX);
        const normalizedY = (worldY - mapConfig.minY) / (mapConfig.maxY - mapConfig.minY);
        
        // Convert to canvas coordinates (flip Y axis)
        return {
            x: normalizedX * canvas.width,
            y: (1 - normalizedY) * canvas.height
        };
    }
    
    // ========================================
    // UI Updates
    // ========================================
    
    updateRoundInfo() {
        document.getElementById('roundNumber').textContent = this.roundData.roundNumber;
        document.getElementById('mapName').textContent = this.currentMap;
        document.getElementById('roundWinner').textContent = this.roundData.winner || 'Unknown';
        
        // Update events list
        this.updateEventsList();
    }
    
    updateTimeline() {
        const slider = document.getElementById('timelineSlider');
        slider.max = this.ticks.length - 1;
        slider.value = this.currentTick;
        
        document.getElementById('timelineDisplay').textContent = 
            `${this.currentTick} / ${this.ticks.length - 1} ticks`;
        document.getElementById('currentTick').textContent = this.currentTick;
    }
    
    updatePlayerList(tick) {
        const ctPlayers = [];
        const tPlayers = [];
        
        Object.entries(tick.players).forEach(([name, data]) => {
            const playerInfo = {
                name: name,
                health: data.health,
                isAlive: data.isAlive,
                weapon: data.weapon || ''
            };
            
            if (data.team === 'CT') {
                ctPlayers.push(playerInfo);
            } else if (data.team === 'TERRORIST') {
                tPlayers.push(playerInfo);
            }
        });
        
        // Sort by alive status (alive first)
        ctPlayers.sort((a, b) => b.isAlive - a.isAlive);
        tPlayers.sort((a, b) => b.isAlive - a.isAlive);
        
        // Update CT players
        const ctContainer = document.getElementById('ctPlayers');
        ctContainer.innerHTML = ctPlayers.map(p => this.createPlayerHTML(p)).join('');
        
        // Update T players
        const tContainer = document.getElementById('tPlayers');
        tContainer.innerHTML = tPlayers.map(p => this.createPlayerHTML(p)).join('');
    }
    
    createPlayerHTML(player) {
        const statusClass = player.isAlive ? 'alive' : 'dead';
        const healthPercent = player.isAlive ? player.health : 0;
        const weaponDisplay = player.weapon ? `<small style="color: #718096;">${player.weapon}</small>` : '';
        
        return `
            <div class="player-item ${statusClass}">
                <div>
                    <span class="player-name">${player.name}</span>
                    ${weaponDisplay}
                </div>
                <div class="player-health">
                    <div class="health-bar">
                        <div class="health-fill" style="width: ${healthPercent}%"></div>
                    </div>
                    <span class="health-text">${player.isAlive ? player.health : '💀'}</span>
                </div>
            </div>
        `;
    }
    
    updateEventsList() {
        const container = document.getElementById('eventsList');
        container.innerHTML = this.roundData.kills.map((kill, idx) => {
            const hsText = kill.headshot ? ' [HS]' : '';
            return `
                <div class="event-item event-kill">
                    <div class="event-time">Kill #${idx + 1}</div>
                    <div class="event-desc">
                        ${kill.killerName} [${kill.weapon}]${hsText} → ${kill.victimName}
                    </div>
                </div>
            `;
        }).join('');
    }
    
    checkEvents() {
        // Check if any events occurred at current tick
        this.events.forEach(event => {
            if (event.tick === this.currentTick) {
                this.showEventNotification(event);
            }
        });
    }
    
    showEventNotification(event) {
        // Could add toast notifications here
        console.log('Event:', event);
    }
}

// Initialize player when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new RoundPlayer();
});

