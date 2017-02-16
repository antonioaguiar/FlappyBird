package br.eti.aaguiar.android.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture[] passaros;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture fundo;
    private Texture gameOver;
    private Circle passaroCirculo;
    private Rectangle canoTopoRet;
    private Rectangle canoBaixoRet;
    //private ShapeRenderer shape;

    //variáveis de configuração
    private int larguraDispositivo;
    private int alturaDispositivo;
    private float variacao = 0;
    private int velocidadeQueda = 0;
    private int posicaoInicialVertical;
    private Random numeroRandomico;
    private float posicaoMovimentoCanoHoriz;
    private float espacoEntreCanos;
    private float espacoEntreCanosRandom;
    private float deltaTime;
    private int estadoJogo = 0;  // 0 = não iniciado, 1 = iniciado, 2 = game over
    private boolean marcouScore = false;

    private BitmapFont fontScore;
    private BitmapFont mensagem;
    private int score;

    private OrthographicCamera camera;
    private Viewport viewport;
    private final int VIRTUAL_WIDTH = 768;
    private final int VIRTUAL_HEIGTH = 1024;

    @Override
    public void create() {
        batch = new SpriteBatch();

        passaros = new Texture[3];
        passaros[0] = new Texture("passaro1.png");  //desce
        passaros[1] = new Texture("passaro2.png");  //plana
        passaros[2] = new Texture("passaro3.png");  //sobe
        gameOver = new Texture("game_over.png");
        passaroCirculo = new Circle();
        canoBaixoRet = new Rectangle();
        canoTopoRet = new Rectangle();
        //shape = new ShapeRenderer();

        fundo = new Texture("fundo.png");

        canoBaixo = new Texture("cano_baixo.png");
        canoTopo = new Texture("cano_topo.png");

        //tratamento para diversas resolucoes
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGTH / 2, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGTH, camera);
        //

        larguraDispositivo = VIRTUAL_WIDTH; //Gdx.graphics.getWidth();
        alturaDispositivo = VIRTUAL_HEIGTH;  //Gdx.graphics.getHeight();
        posicaoInicialVertical = alturaDispositivo / 2;

        posicaoMovimentoCanoHoriz = larguraDispositivo;
        numeroRandomico = new Random();

        espacoEntreCanos = 400;

        fontScore = new BitmapFont();
        fontScore.setColor(Color.WHITE);
        fontScore.getData().setScale(6);

        mensagem = new BitmapFont();
        mensagem.setColor(Color.WHITE);
        mensagem.getData().setScale(3);

        score = 0;
    }

    //metodo que é chamado de tempos em tempos onde é possível criar animações do jogo
    @Override
    public void render() {
        camera.update();
        //LIMPAR FRAMES ANTERIORES
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);

        float posicaoMovimentoVerticalCanoTopo = alturaDispositivo / 2 + espacoEntreCanos / 2 + espacoEntreCanosRandom;
        float posicaoMovimentoVerticalCanoBaixo = alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + espacoEntreCanosRandom;

        if (variacao > 2)
            variacao = 0;

        deltaTime = Gdx.graphics.getDeltaTime();
        variacao += deltaTime * 10;

        if (estadoJogo == 0) {  //jogo não iniciado

            if (Gdx.input.justTouched()) {
                estadoJogo = 1;
            }

        } else {

            velocidadeQueda++;
            if (posicaoInicialVertical > 0 || velocidadeQueda < 0)
                posicaoInicialVertical = posicaoInicialVertical - velocidadeQueda;

            if (estadoJogo == 1) { // jogo iniciado
                posicaoMovimentoCanoHoriz -= deltaTime * 200;

                if (Gdx.input.justTouched()) {
                    velocidadeQueda = -15;
                }

                //verifica se o cano saiu inteiramente da tela.
                if (posicaoMovimentoCanoHoriz < -canoTopo.getWidth()) {
                    posicaoMovimentoCanoHoriz = larguraDispositivo;
                    espacoEntreCanosRandom = numeroRandomico.nextInt(400) - 200;
                    marcouScore = false;
                }

                //verifica pontuação
                if (posicaoMovimentoCanoHoriz < 120) {
                    if (!marcouScore) {
                        score++;
                        marcouScore = true;
                    }
                }
            } else {  //Game Over
                // Gdx.app.log("GAMEOVER", "GAME OVER!");
                if (Gdx.input.justTouched()) {
                    estadoJogo = 0;
                    score = 0;
                    velocidadeQueda = 0;
                    posicaoInicialVertical = alturaDispositivo / 2;
                    posicaoMovimentoCanoHoriz = larguraDispositivo;
                }
            }

        }

        //configurar  projecao da camera
        batch.setProjectionMatrix(camera.combined);

        // a ordem dos elementos interfere no resultado
        batch.begin();
        batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
        batch.draw(canoTopo, posicaoMovimentoCanoHoriz, posicaoMovimentoVerticalCanoTopo);
        batch.draw(canoBaixo, posicaoMovimentoCanoHoriz, posicaoMovimentoVerticalCanoBaixo);

        //passaro
        batch.draw(passaros[(int) variacao], 120, posicaoInicialVertical);

        fontScore.draw(batch, String.valueOf(score), larguraDispositivo / 2 - 20, alturaDispositivo - 50);
        if (estadoJogo == 2) {
            batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo / 2);
            mensagem.draw(batch, "Toque para reiniciar", larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo / 2 - gameOver.getHeight() / 2);
        }
        batch.end();

        int posXcircle = 120 + passaros[0].getWidth() / 2;
        int posYcircle = posicaoInicialVertical + passaros[0].getHeight() / 2;
        int radCircle = passaros[0].getWidth() / 2;

        passaroCirculo.set(posXcircle, posYcircle, radCircle);
        canoTopoRet = new Rectangle(posicaoMovimentoCanoHoriz, posicaoMovimentoVerticalCanoTopo, canoTopo.getWidth(), canoTopo.getHeight());
        canoBaixoRet = new Rectangle(posicaoMovimentoCanoHoriz, posicaoMovimentoVerticalCanoBaixo, canoBaixo.getWidth(), canoBaixo.getHeight());

        //desenhar formas para detectar colisão
        /*
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.circle(passaroCirculo.x, passaroCirculo.y, passaroCirculo.radius);
        //shape.setColor(Color.RED);
        shape.rect(canoTopoRet.x, canoTopoRet.y, canoTopoRet.width, canoTopoRet.height);
        shape.rect(canoBaixoRet.x, canoBaixoRet.y, canoBaixoRet.width, canoBaixoRet.height);
        shape.setColor(Color.RED);
        shape.end();
        */
        //teste de colisão
        //colisao entre passaro e partes do cano
        if (Intersector.overlaps(passaroCirculo, canoBaixoRet) || Intersector.overlaps(passaroCirculo, canoTopoRet)
                || posicaoInicialVertical <= 0 || posicaoInicialVertical >= alturaDispositivo) {
            //Gdx.app.log("COLISAO", "Houve colisão!");
            estadoJogo = 2;
        }

    }

    @Override
    public void resize(int width, int heigth) {
        viewport.update(width, heigth);
    }

}
