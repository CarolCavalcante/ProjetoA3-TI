import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

// ===================== ENUMS =====================
enum Perfil {
    ADMINISTRADOR,
    GERENTE,
    COLABORADOR
}

enum Status {
    PLANEJANDO,
    EM_ANDAMENTO,
    CONCLUIDO,
    CANCELADO,
    ATRASADO
}

enum Prioridade {
    ALTA,
    MEDIA,
    NORMAL,
    BAIXA
}

// ===================== CLASSES =====================
class Usuario {
    private String nome;
    private String email;
    private String login;
    private String senha;
    private String cargo;
    private Perfil perfil;
    private List<Projeto> projetos = new ArrayList<>();

    public Usuario(String nome, String email, String login, String senha, String cargo, Perfil perfil) {
        this.nome = nome;
        this.email = email;
        this.login = login;
        this.senha = senha;
        this.cargo = cargo;
        this.perfil = perfil;
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getLogin() { return login; }
    public String getSenha() { return senha; }
    public String getCargo() { return cargo; }
    public Perfil getPerfil() { return perfil; }
    public List<Projeto> getProjetos() { return projetos; }

    public void adicionarProjeto(Projeto p) {
        if (projetos.size() >= 4) {
            System.out.println("❌ Usuário " + nome + " já está em 4 projetos simultâneos. Finalize um antes de entrar em outro.");
        } else {
            projetos.add(p);
        }
    }
}

class Tarefa {
    private String descricao;
    private LocalDate inicio;
    private LocalDate fimPrevisto;
    private LocalDate fimReal;
    private Status status;
    private Prioridade prioridade;
    private Usuario responsavel;
    private String motivoCancelamento;
    private String motivoAtraso;

    public Tarefa(String descricao, LocalDate inicio, LocalDate fimPrevisto, Prioridade prioridade, Usuario responsavel) {
        this.descricao = descricao;
        this.inicio = inicio;
        this.fimPrevisto = fimPrevisto;
        this.status = Status.PLANEJANDO;
        this.prioridade = prioridade;
        this.responsavel = responsavel;
    }

    public void concluir() {
        this.fimReal = LocalDate.now();
        if (fimReal.isAfter(fimPrevisto)) {
            this.status = Status.ATRASADO;
            System.out.println("⚠️ Tarefa concluída com atraso. Informe o motivo:");
            Scanner sc = new Scanner(System.in);
            motivoAtraso = sc.nextLine();
        } else {
            this.status = Status.CONCLUIDO;
        }
        notificar("conclusão");
    }

    public void cancelar() {
        this.status = Status.CANCELADO;
        System.out.println("❌ Informe o motivo do cancelamento:");
        Scanner sc = new Scanner(System.in);
        motivoCancelamento = sc.nextLine();
        notificar("cancelamento");
    }

    public void verificarAtraso() {
        if (status != Status.CONCLUIDO && status != Status.CANCELADO && LocalDate.now().isAfter(fimPrevisto)) {
            this.status = Status.ATRASADO;
        }
    }

    private void notificar(String tipo) {
        System.out.println("\n=== Notificação de E-mail (simulada) ===");
        System.out.println("Para: " + responsavel.getEmail());
        if (tipo.equals("conclusão")) {
            System.out.println("Assunto: Obrigado por sua contribuição!");
            System.out.println("Mensagem: Sua tarefa \"" + descricao + "\" foi concluída. Obrigado!");
        } else {
            System.out.println("Assunto: Tarefa cancelada");
            System.out.println("Mensagem: Sua tarefa \"" + descricao + "\" foi cancelada. Motivo: " + motivoCancelamento);
        }
        System.out.println("-----------------------------------");

        System.out.println("Para: Administrador e Gerente do projeto (simulado)");
        System.out.println("Mensagem: O colaborador " + responsavel.getNome() + " atualizou a tarefa \"" + descricao + "\" com status " + status);
        System.out.println("===================================\n");
    }

    public String exportarCSV(String projeto, Usuario admin, Usuario gerente) {
        return projeto + ";" + admin.getNome() + ";" + gerente.getNome() + ";" +
                responsavel.getNome() + ";" + descricao + ";" + inicio + ";" +
                fimPrevisto + ";" + status + ";" +
                (motivoCancelamento != null ? motivoCancelamento : motivoAtraso != null ? motivoAtraso : "");
    }

    @Override
    public String toString() {
        return descricao + " | Resp: " + responsavel.getNome() + " | Status: " + status +
               (status == Status.ATRASADO ? " (ATRASADO)" : "") +
               (status == Status.CANCELADO ? " | Motivo: " + motivoCancelamento : "");
    }
}

class Projeto {
    private String nome;
    private String descricao;
    private Usuario administrador;
    private Usuario gerente;
    private List<Usuario> equipe = new ArrayList<>();
    private List<Tarefa> tarefas = new ArrayList<>();

    public Projeto(String nome, String descricao, Usuario administrador, Usuario gerente) {
        this.nome = nome;
        this.descricao = descricao;
        this.administrador = administrador;
        this.gerente = gerente;
    }

    public void adicionarMembro(Usuario u) {
        if (u.getProjetos().size() >= 4) {
            System.out.println("❌ Usuário " + u.getNome() + " já está em 4 projetos. Não foi adicionado.");
        } else {
            equipe.add(u);
            u.adicionarProjeto(this);
        }
    }

    public void adicionarTarefa(Tarefa t) {
        tarefas.add(t);
    }

    public List<Tarefa> getTarefas() { return tarefas; }
    public Usuario getAdministrador() { return administrador; }
    public Usuario getGerente() { return gerente; }
    public String getNome() { return nome; }
    public List<Usuario> getEquipe() { return equipe; }

    public void verificarStatusGeral() {
        for (Tarefa t : tarefas) t.verificarAtraso();
    }

    public void exportarCSV() {
        try (FileWriter writer = new FileWriter("projetos_export.csv", true)) {
            for (Tarefa t : tarefas) {
                writer.write(t.exportarCSV(nome, administrador, gerente) + "\n");
            }
            System.out.println("✅ Dados exportados para projetos_export.csv");
        } catch (IOException e) {
            System.out.println("Erro ao exportar CSV: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "\nProjeto: " + nome +
               "\nDescrição: " + descricao +
               "\nAdministrador: " + administrador.getNome() +
               "\nGerente: " + gerente.getNome() +
               "\nEquipe: " + equipe.size() + " membros" +
               "\nTarefas: " + tarefas.size();
    }
}

// ===================== MAIN =====================
public class SistemaFinal {
    private static List<Usuario> usuarios = new ArrayList<>();
    private static List<Projeto> projetos = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        seedUsuarios();
        menuLogin();
    }

    private static void seedUsuarios() {
        usuarios.add(new Usuario("Lucas Silva", "lucas.silva@email.com", "lucas.silva", "Padrao123", "PO", Perfil.ADMINISTRADOR));
        usuarios.add(new Usuario("Carol Cavalcante", "carol.cavalcante@email.com", "carol.cavalcante", "Padrao123", "Gerente", Perfil.GERENTE));
        usuarios.add(new Usuario("Thamiris Marie", "thamiris.marie@email.com", "thamiris.marie", "Padrao123", "Analista", Perfil.COLABORADOR));
        usuarios.add(new Usuario("Rodrigo Bat", "rodrigo.bat@email.com", "rodrigo.bat", "Padrao123", "Analista", Perfil.COLABORADOR));
    }

    private static void menuLogin() {
        System.out.println("\n===== LOGIN =====");
        System.out.print("Login ou E-mail: ");
        String login = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        Usuario usuario = autenticar(login, senha);
        if (usuario == null) {
            System.out.println("❌ Credenciais inválidas.");
            // Offer retry
            menuLogin();
        } else {
            System.out.println("✅ Bem-vindo, " + usuario.getNome());
            menuPrincipal(usuario);
        }
    }

    private static Usuario autenticar(String loginOuEmail, String senha) {
        for (Usuario u : usuarios) {
            if ((u.getLogin().equalsIgnoreCase(loginOuEmail) || u.getEmail().equalsIgnoreCase(loginOuEmail))
                    && u.getSenha().equals(senha)) {
                return u;
            }
        }
        return null;
    }

    private static void menuPrincipal(Usuario usuario) {
        boolean rodando = true;
        while (rodando) {
            System.out.println("\n===== MENU PRINCIPAL =====");
            System.out.println("1. Criar Projeto");
            System.out.println("2. Listar Projetos");
            System.out.println("3. Abrir Projeto");
            System.out.println("4. Exportar Projetos para CSV");
            System.out.println("5. Sair");
            System.out.print("Escolha: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1 -> criarProjeto(usuario);
                case 2 -> listarProjetos();
                case 3 -> abrirProjeto(usuario);
                case 4 -> exportarProjetos();
                case 5 -> { rodando = false; System.out.println(\"Até mais!\"); }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void criarProjeto(Usuario usuario) {
        System.out.print("Nome do projeto: ");
        String nome = scanner.nextLine();
        System.out.print("Descrição: ");
        String desc = scanner.nextLine();

        System.out.println("Escolha um gerente pelo número:");
        List<Usuario> gerentes = new ArrayList<>();
        for (Usuario u : usuarios) if (u.getPerfil() == Perfil.GERENTE) gerentes.add(u);
        for (int i = 0; i < gerentes.size(); i++) System.out.println(i + " - " + gerentes.get(i).getNome());
        int escolha = 0;
        if (!gerentes.isEmpty()) {
            escolha = Integer.parseInt(scanner.nextLine());
        }
        Usuario gerente = gerentes.isEmpty() ? usuario : gerentes.get(escolha);

        Projeto projeto = new Projeto(nome, desc, usuario, gerente);
        projetos.add(projeto);
        usuario.adicionarProjeto(projeto);

        System.out.println("✅ Projeto criado: " + nome);
        menuProjeto(projeto, usuario);
    }

    private static void listarProjetos() {
        if (projetos.isEmpty()) {
            System.out.println("Nenhum projeto cadastrado.");
            return;
        }
        for (int i = 0; i < projetos.size(); i++) {
            Projeto p = projetos.get(i);
            System.out.println(i + " - " + p.getNome() + " | Gerente: " + p.getGerente().getNome() + " | Membros: " + p.getEquipe().size());
        }
    }

    private static void abrirProjeto(Usuario usuario) {
        listarProjetos();
        System.out.print("Número do projeto para abrir: ");
        int idx = Integer.parseInt(scanner.nextLine());
        if (idx < 0 || idx >= projetos.size()) {
            System.out.println("Projeto inválido.");
            return;
        }
        Projeto p = projetos.get(idx);
        menuProjeto(p, usuario);
    }

    private static void menuProjeto(Projeto projeto, Usuario usuario) {
        boolean rodando = true;
        while (rodando) {
            System.out.println("\n===== PROJETO: " + projeto.getNome() + " =====");
            System.out.println("1. Adicionar membro (Admin/Gerente)");
            System.out.println("2. Adicionar tarefa");
            System.out.println("3. Listar tarefas");
            System.out.println("4. Voltar");
            System.out.print("Escolha: ");
            int opcao = Integer.parseInt(scanner.nextLine());

            switch (opcao) {
                case 1 -> {
                    if (usuario.getPerfil() == Perfil.COLABORADOR) {
                        System.out.println("Você não tem permissão para adicionar membros.");
                    } else {
                        adicionarMembro(projeto);
                    }
                }
                case 2 -> adicionarTarefa(projeto);
                case 3 -> listarTarefas(projeto);
                case 4 -> rodando = false;
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void adicionarMembro(Projeto projeto) {
        System.out.println("Selecione usuário para adicionar:");
        for (int i = 0; i < usuarios.size(); i++) {
            System.out.println(i + " - " + usuarios.get(i).getNome() + " (" + usuarios.get(i).getPerfil() + ")");
        }
        int escolha = Integer.parseInt(scanner.nextLine());
        if (escolha < 0 || escolha >= usuarios.size()) {
            System.out.println("Escolha inválida.");
            return;
        }
        projeto.adicionarMembro(usuarios.get(escolha));
        System.out.println("Membro adicionado!");
    }

    private static void adicionarTarefa(Projeto projeto) {
        if (projeto.getEquipe().isEmpty()) {
            System.out.println("Não há membros na equipe. Adicione membros primeiro.");
            return;
        }
        System.out.print("Descrição da tarefa: ");
        String desc = scanner.nextLine();

        System.out.println("Selecione o responsável:");
        for (int i = 0; i < projeto.getEquipe().size(); i++) {
            System.out.println(i + " - " + projeto.getEquipe().get(i).getNome());
        }
        int resp = Integer.parseInt(scanner.nextLine());
        Usuario responsavel = projeto.getEquipe().get(resp);

        System.out.print("Data de início (dd/MM/yyyy): ");
        String inicioStr = scanner.nextLine();
        System.out.print("Data de término prevista (dd/MM/yyyy): ");
        String fimStr = scanner.nextLine();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate inicio = LocalDate.parse(inicioStr, fmt);
        LocalDate fim = LocalDate.parse(fimStr, fmt);

        System.out.println("Prioridade (1=ALTA,2=MÉDIA,3=NORMAL,4=BAIXA): ");
        int p = Integer.parseInt(scanner.nextLine());
        Prioridade prioridade = Prioridade.NORMAL;
        if (p == 1) prioridade = Prioridade.ALTA;
        else if (p == 2) prioridade = Prioridade.MEDIA;
        else if (p == 4) prioridade = Prioridade.BAIXA;

        Tarefa tarefa = new Tarefa(desc, inicio, fim, prioridade, responsavel);
        projeto.adicionarTarefa(tarefa);
        System.out.println("Tarefa adicionada!");
    }

    private static void listarTarefas(Projeto projeto) {
        if (projeto.getTarefas().isEmpty()) {
            System.out.println("Nenhuma tarefa cadastrada.");
            return;
        }
        for (int i = 0; i < projeto.getTarefas().size(); i++) {
            System.out.println(i + " - " + projeto.getTarefas().get(i));
        }
        System.out.print("Selecione tarefa para gerenciar ou -1 para voltar: ");
        int escolha = Integer.parseInt(scanner.nextLine());
        if (escolha >= 0 && escolha < projeto.getTarefas().size()) {
            menuTarefa(projeto.getTarefas().get(escolha));
        }
    }

    private static void menuTarefa(Tarefa tarefa) {
        boolean rodando = true;
        while (rodando) {
            System.out.println("\n===== TAREFA: " + tarefa + " =====");
            System.out.println("1. Concluir");
            System.out.println("2. Cancelar");
            System.out.println("3. Voltar");
            System.out.print("Escolha: ");
            int opcao = Integer.parseInt(scanner.nextLine());
            switch (opcao) {
                case 1 -> tarefa.concluir();
                case 2 -> tarefa.cancelar();
                case 3 -> rodando = false;
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void exportarProjetos() {
        for (Projeto p : projetos) p.exportarCSV();
    }
}
