import com.sun.jna.Library;
import com.sun.jna.Native;

public class ControleTeclasJogoComJNA {

    public interface Crt extends Library {
        Crt INSTANCE = Native.load("msvcrt", Crt.class);
        int _kbhit();
        int _getch();
    }

    static boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
    static boolean tecladoPreparado = false;

    // Retorna true se alguma tecla foi pressionada. NÃO espera: se nenhuma
    // tecla foi apertada, retorna false na hora e o jogo continua.
    public static boolean pressionouTecla() {
        if (windows) {
            return Crt.INSTANCE._kbhit() != 0;
        }
        prepararTecladoMacLinux();
        try {
            return System.in.available() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Retorna o código ASCII da tecla pressionada (ex.: 'a', 'w', 27 = ESC).
    // Se nenhuma tecla foi apertada, ESPERA até o usuário apertar uma.
    public static int obtemTeclaPressionada() {
        if (windows) {
            return Crt.INSTANCE._getch();
        }
        prepararTecladoMacLinux();
        try {
            return System.in.read();
        } catch (Exception e) {
            return -1;
        }
    }

    // Mostra "Pressione uma tecla para continuar..." e PARA o programa
    // até o jogador apertar qualquer tecla.
    //
    // Exemplo:
    //   System.out.println("GAME OVER");
    //   esperarTecla();
    public static void esperarTecla() {
        System.out.println("Pressione uma tecla para continuar...");
        obtemTeclaPressionada(); // fica esperando a tecla
    }

    // Mac/Linux: faz o terminal entregar cada tecla na hora (sem ENTER) e
    // sem mostrar a letra na tela. Quando o programa termina, volta ao normal.
    static void prepararTecladoMacLinux() {
        if (tecladoPreparado) {
            return;
        }
        tecladoPreparado = true;
        executarStty("-icanon -echo -icrnl min 1"); // -icrnl: ENTER vira 13, igual ao Windows
        Runtime.getRuntime().addShutdownHook(new Thread(() -> executarStty("sane")));
    }

    static void executarStty(String opcoes) {
        try {
            new ProcessBuilder("sh", "-c", "stty " + opcoes + " < /dev/tty").start().waitFor();
        } catch (Exception e) {
            // se não conseguir, o teclado só vai funcionar com ENTER
        }
    }
    // ===================================================================

    public static void limparConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // Pausa o programa por um tempo, em milissegundos (1000 = 1 segundo).
    public static void esperar(int milissegundos) {
        try {
            Thread.sleep(milissegundos);
        } catch (InterruptedException e) {
            // não faz nada: se a espera for interrompida, o programa só continua
        }
    }

    public static void desenharTela(int x, int y, int largura, int altura) {
        for (int i = 0; i < altura; i++) {
            for (int j = 0; j < largura; j++) {
                if (i == y && j == x) {
                    System.out.print('H');
                } else {
                    System.out.print('.');
                }
            }
            System.out.println();
        }
        System.out.println("Use A, D, W, S para mover o personagem. Pressione ESC para sair.");
    }

    public static void main(String[] args) {
        int largura = 40;
        int altura = 10;
        int posX = 10;
        int posY = 5;

        limparConsole();
        System.out.println("Mova o personagem H com W, A, S, D. ESC para sair.");
        esperarTecla();

        while (true) {
            // Lógica para detectar o movimento
            if (pressionouTecla()) {
                int tecla = obtemTeclaPressionada();
                switch (tecla) {
                    case 'a':
                    case 'A':
                        posX--;
                        break;
                    case 'd':
                    case 'D':
                        posX++;
                        break;
                    case 'w':
                    case 'W':
                        posY--;
                        break;
                    case 's':
                    case 'S':
                        posY++;
                        break;
                    case 27: // Código ASCII para a tecla ESC
                        return; // Sai do loop e termina o programa
                }
            }

            // Não deixa o personagem sair da tela
            if (posX < 0) posX = 0;
            if (posX > largura - 1) posX = largura - 1;
            if (posY < 0) posY = 0;
            if (posY > altura - 1) posY = altura - 1;

            // Lógica do jogo (desenhar a tela)
            limparConsole();
            desenharTela(posX, posY, largura, altura);

            // Controla a velocidade do jogo
            esperar(100); // espera 100 milissegundos -> 0,1 segundo
        }
    }
}
