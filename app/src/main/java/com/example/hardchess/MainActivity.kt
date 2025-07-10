package com.example.hardchess

import android.graphics.Color
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hardchess.databinding.ActivityMainBinding
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.view.animation.OvershootInterpolator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var secilenSatir: Int = -1
    private var secilenSutun: Int = -1
    private var secilenTas: ImageView? = null
    private var secilenTasTipi: ChessPiece? = null
    private var oyunSirasi = "Beyaz"
    private val vurgulananKareler = mutableListOf<ImageView>()
    private var moveCounter = 1

    private val tahtaKaresi = Array(8) { Array<ChessPiece?>(8) { null } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup button listeners
        setupButtonListeners()

        binding.chessboardGridLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.chessboardGridLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                setupChessboard()
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupButtonListeners() {
        binding.btnRestart.setOnClickListener {
            animateButtonClick(binding.btnRestart)
            restartGame()
        }

        binding.btnUndo.setOnClickListener {
            animateButtonClick(binding.btnUndo)
            // Undo functionality can be implemented here
            Toast.makeText(this, "Geri alma özelliği yakında eklenecek", Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateButtonClick(button: ImageView) {
        val scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1.0f, 0.9f)
        val scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.9f, 1.0f)
        val scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1.0f, 0.9f)
        val scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.9f, 1.0f)

        val animatorSet = AnimatorSet()
        animatorSet.duration = 150
        animatorSet.interpolator = OvershootInterpolator()
        animatorSet.playTogether(scaleDown, scaleDownY)
        animatorSet.play(scaleUp).with(scaleUpY).after(scaleDown)
        animatorSet.start()
    }

    private fun restartGame() {
        // Reset game state
        secilenSatir = -1
        secilenSutun = -1
        secilenTas = null
        secilenTasTipi = null
        oyunSirasi = "Beyaz"
        moveCounter = 1
        vurgulananKareler.clear()

        // Update UI
        updateTurnDisplay()
        updateMoveCounter()

        // Recreate chessboard
        setupChessboard()

        Toast.makeText(this, "Oyun yeniden başlatıldı", Toast.LENGTH_SHORT).show()
    }

    private fun updateTurnDisplay() {
        val turnText = if (oyunSirasi == "Beyaz") "Beyaz Oyuncunun Sırası" else "Siyah Oyuncunun Sırası"
        binding.turnText.text = turnText

        // Update player icon
        val iconRes = if (oyunSirasi == "Beyaz") R.drawable.beyaz_sah else R.drawable.siyah_sah
        binding.currentPlayerIcon.setImageResource(iconRes)
    }

    private fun updateMoveCounter() {
        binding.moveCounter.text = moveCounter.toString()
    }

    private fun setupChessboard() {
        val chessboardGridLayout = binding.chessboardGridLayout
        val gridWidth = chessboardGridLayout.width
        val kareBoyutu = gridWidth / 8

        chessboardGridLayout.removeAllViews()

        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val squareImageView = ImageView(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = kareBoyutu
                        height = kareBoyutu
                        rowSpec = GridLayout.spec(row)
                        columnSpec = GridLayout.spec(col)
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true
                    contentDescription = "Satranç Karesi R:$row C:$col"

                    // Enhanced square styling
                    if ((row + col) % 2 == 0) {
                        setBackgroundResource(R.drawable.chess_square_light)
                    } else {
                        setBackgroundResource(R.drawable.chess_square_dark)
                    }

                    // Add subtle shadow effect
                    elevation = 2f

                    setOnClickListener {
                        onSquareClicked(this, row, col)
                    }
                }
                chessboardGridLayout.addView(squareImageView)

                secilenTasTipi = null

                when (row) {
                    0 -> when (col) {
                        0, 7 -> secilenTasTipi = ChessPiece(tasTuru.KALE, tasRengi.SIYAH, R.drawable.siyah_kale)
                        1, 6 -> secilenTasTipi = ChessPiece(tasTuru.AT, tasRengi.SIYAH, R.drawable.siyah_at)
                        2, 5 -> secilenTasTipi = ChessPiece(tasTuru.FIL, tasRengi.SIYAH, R.drawable.siyah_fil)
                        3 -> secilenTasTipi = ChessPiece(tasTuru.VEZIR, tasRengi.SIYAH, R.drawable.siyah_vezir)
                        4 -> secilenTasTipi = ChessPiece(tasTuru.SAH, tasRengi.SIYAH, R.drawable.siyah_sah)
                        else -> null
                    }
                    1 -> secilenTasTipi = ChessPiece(tasTuru.PIYON, tasRengi.SIYAH, R.drawable.siyah_piyon)
                    6 -> secilenTasTipi = ChessPiece(tasTuru.PIYON, tasRengi.BEYAZ, R.drawable.beyaz_piyon)
                    7 -> when (col) {
                        0, 7 -> secilenTasTipi = ChessPiece(tasTuru.KALE, tasRengi.BEYAZ, R.drawable.beyaz_kale)
                        1, 6 -> secilenTasTipi = ChessPiece(tasTuru.AT, tasRengi.BEYAZ, R.drawable.beyaz_at)
                        2, 5 -> secilenTasTipi = ChessPiece(tasTuru.FIL, tasRengi.BEYAZ, R.drawable.beyaz_fil)
                        3 -> secilenTasTipi = ChessPiece(tasTuru.VEZIR, tasRengi.BEYAZ, R.drawable.beyaz_vezir)
                        4 -> secilenTasTipi = ChessPiece(tasTuru.SAH, tasRengi.BEYAZ, R.drawable.beyaz_sah)
                        else -> null
                    }
                    else -> null
                }

                secilenTasTipi?.let {
                    squareImageView.setImageResource(it.drawableResId)
                    tahtaKaresi[row][col] = it

                    // Add subtle animation when pieces are placed
                    squareImageView.alpha = 0f
                    squareImageView.animate().alpha(1f).setDuration(300).start()
                }
            }
        }
    }

    private fun onSquareClicked(clickedSquare: ImageView, satir: Int, sutun: Int) {
        temizleVurgulamalar()

        if (secilenSatir == -1 || secilenSutun == -1) {
            val pieceOnSquare = tahtaKaresi[satir][sutun]

            if (pieceOnSquare != null) {
                val requiredColor = if (oyunSirasi == "Beyaz") tasRengi.BEYAZ else tasRengi.SIYAH
                if (pieceOnSquare.color != requiredColor) {
                    Toast.makeText(this, "Sıra ${oyunSirasi} oyuncuda!", Toast.LENGTH_SHORT).show()
                    return
                }

                secilenSatir = satir
                secilenSutun = sutun
                secilenTas = clickedSquare

                // Enhanced selection animation
                clickedSquare.setBackgroundResource(R.drawable.chess_square_selected)
                val scaleAnimation = ObjectAnimator.ofFloat(clickedSquare, "scaleX", 1.0f, 1.1f, 1.0f)
                val scaleAnimationY = ObjectAnimator.ofFloat(clickedSquare, "scaleY", 1.0f, 1.1f, 1.0f)
                val animatorSet = AnimatorSet()
                animatorSet.duration = 300
                animatorSet.playTogether(scaleAnimation, scaleAnimationY)
                animatorSet.start()

                vurgulaGecerliHamleler(satir, sutun, pieceOnSquare)
            } else {
                Toast.makeText(this, "Boş kare seçildi, önce bir taş seçin.", Toast.LENGTH_SHORT).show()
            }
        } else {
            val hedefSatir = satir
            val hedefSutun = sutun
            val sourcePiece = tahtaKaresi[secilenSatir][secilenSutun]

            if (sourcePiece != null) {
                if (isValidMove(secilenSatir, secilenSutun, hedefSatir, hedefSutun, sourcePiece)) {
                    // Animate piece movement
                    val moveAnimation = ObjectAnimator.ofFloat(clickedSquare, "alpha", 0.5f, 1.0f)
                    moveAnimation.duration = 200
                    moveAnimation.start()

                    secilenTas?.setImageResource(0)
                    tahtaKaresi[secilenSatir][secilenSutun] = null
                    secilenTas?.setBackgroundResource(getSquareOriginalDrawable(secilenSatir, secilenSutun))

                    clickedSquare.setImageResource(sourcePiece.drawableResId)
                    tahtaKaresi[hedefSatir][hedefSutun] = sourcePiece

                    // Update turn and move counter
                    oyunSirasi = if (oyunSirasi == "Beyaz") "Siyah" else "Beyaz"
                    if (oyunSirasi == "Beyaz") {
                        moveCounter++
                        updateMoveCounter()
                    }
                    updateTurnDisplay()

                    Toast.makeText(this, "Sıra ${oyunSirasi} oyuncuda.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Geçersiz hareket!", Toast.LENGTH_SHORT).show()
                    secilenTas?.setBackgroundResource(getSquareOriginalDrawable(secilenSatir, secilenSutun))
                }
            }

            secilenSatir = -1
            secilenSutun = -1
            secilenTas = null
            secilenTasTipi = null
        }
    }

    private fun isValidMove(
        baslangicSatir: Int,
        baslangicSutun: Int,
        hedefSatir: Int,
        hedefSutun: Int,
        tas: ChessPiece
    ): Boolean {
        if (hedefSatir < 0 || hedefSatir >= 8 || hedefSutun < 0 || hedefSutun >= 8) {
            return false
        }

        if (baslangicSatir == hedefSatir && baslangicSutun == hedefSutun) {
            return false
        }

        val hedefdekiTas = tahtaKaresi[hedefSatir][hedefSutun]
        if (hedefdekiTas != null && hedefdekiTas.color == tas.color) {
            return false
        }

        return when (tas.type) {
            tasTuru.PIYON -> {
                val piyonyon = if (tas.color == tasRengi.BEYAZ) -1 else 1
                val baslangicSira = if (tas.color == tasRengi.BEYAZ) 6 else 1

                if (hedefSutun == baslangicSutun && hedefSatir == baslangicSatir + piyonyon) {
                    return tahtaKaresi[hedefSatir][hedefSutun] == null
                }

                if (baslangicSatir == baslangicSira && hedefSutun == baslangicSutun && hedefSatir == baslangicSatir + 2 * piyonyon) {
                    return tahtaKaresi[baslangicSatir + piyonyon][baslangicSutun] == null && tahtaKaresi[hedefSatir][hedefSutun] == null
                }

                if (Math.abs(hedefSutun - baslangicSutun) == 1 && hedefSatir == baslangicSatir + piyonyon) {
                    return hedefdekiTas != null
                }
                false
            }
            tasTuru.AT -> {
                val satirFarki = Math.abs(hedefSatir - baslangicSatir)
                val sutunFarki = Math.abs(hedefSutun - baslangicSutun)
                val isValidKnightMove = (satirFarki == 2 && sutunFarki == 1) || (satirFarki == 1 && sutunFarki == 2)
                return isValidKnightMove
            }
            tasTuru.KALE -> {
                val dikeyHareket = baslangicSutun == hedefSutun
                val yatayHareket = baslangicSatir == hedefSatir

                if (!dikeyHareket && !yatayHareket) {
                    return false
                }

                if (dikeyHareket) {
                    if (hedefSatir > baslangicSatir) {
                        for (i in baslangicSatir + 1 until hedefSatir) {
                            if (tahtaKaresi[i][baslangicSutun] != null) {
                                return false
                            }
                        }
                    } else {
                        for (i in baslangicSatir - 1 downTo hedefSatir + 1) {
                            if (tahtaKaresi[i][baslangicSutun] != null) {
                                return false
                            }
                        }
                    }
                } else {
                    if (hedefSutun > baslangicSutun) {
                        for (i in baslangicSutun + 1 until hedefSutun) {
                            if (tahtaKaresi[baslangicSatir][i] != null) {
                                return false
                            }
                        }
                    } else {
                        for (i in baslangicSutun - 1 downTo hedefSutun + 1) {
                            if (tahtaKaresi[baslangicSatir][i] != null) {
                                return false
                            }
                        }
                    }
                }
                true
            }
            tasTuru.FIL -> {
                val satirFarki = Math.abs(hedefSatir - baslangicSatir)
                val sutunFarki = Math.abs(hedefSutun - baslangicSutun)

                if (satirFarki != sutunFarki || satirFarki == 0) {
                    return false
                }

                val satirYon = if (hedefSatir > baslangicSatir) 1 else -1
                val sutunYon = if (hedefSutun > baslangicSutun) 1 else -1

                var currentRow = baslangicSatir + satirYon
                var currentCol = baslangicSutun + sutunYon

                while (currentRow != hedefSatir || currentCol != hedefSutun) {
                    if (tahtaKaresi[currentRow][currentCol] != null) {
                        return false
                    }
                    currentRow += satirYon
                    currentCol += sutunYon
                }
                true
            }
            tasTuru.VEZIR -> {
                val dikeyHareket = baslangicSutun == hedefSutun
                val yatayHareket = baslangicSatir == hedefSatir
                val caprazHareket = Math.abs(hedefSatir - baslangicSatir) == Math.abs(hedefSutun - baslangicSutun)

                if (!dikeyHareket && !yatayHareket && !caprazHareket) {
                    return false
                }

                if (dikeyHareket || yatayHareket) {
                    if (dikeyHareket) {
                        if (hedefSatir > baslangicSatir) {
                            for (i in baslangicSatir + 1 until hedefSatir) {
                                if (tahtaKaresi[i][baslangicSutun] != null) {
                                    return false
                                }
                            }
                        } else {
                            for (i in baslangicSatir - 1 downTo hedefSatir + 1) {
                                if (tahtaKaresi[i][baslangicSutun] != null) {
                                    return false
                                }
                            }
                        }
                    } else {
                        if (hedefSutun > baslangicSutun) {
                            for (i in baslangicSutun + 1 until hedefSutun) {
                                if (tahtaKaresi[baslangicSatir][i] != null) {
                                    return false
                                }
                            }
                        } else {
                            for (i in baslangicSutun - 1 downTo hedefSutun + 1) {
                                if (tahtaKaresi[baslangicSatir][i] != null) {
                                    return false
                                }
                            }
                        }
                    }
                } else if (caprazHareket) {
                    val satirYon = if (hedefSatir > baslangicSatir) 1 else -1
                    val sutunYon = if (hedefSutun > baslangicSutun) 1 else -1

                    var currentRow = baslangicSatir + satirYon
                    var currentCol = baslangicSutun + sutunYon

                    while (currentRow != hedefSatir || currentCol != hedefSutun) {
                        if (tahtaKaresi[currentRow][currentCol] != null) {
                            return false
                        }
                        currentRow += satirYon
                        currentCol += sutunYon
                    }
                }
                true
            }
            tasTuru.SAH -> {
                val satirFarki = Math.abs(hedefSatir - baslangicSatir)
                val sutunFarki = Math.abs(hedefSutun - baslangicSutun)
                return satirFarki <= 1 && sutunFarki <= 1
            }
            else -> false
        }
    }

    private fun vurgulaGecerliHamleler(baslangicSatir: Int, baslangicSutun: Int, tas: ChessPiece) {
        val chessboardGridLayout = binding.chessboardGridLayout

        for (row in 0 until 8) {
            for (col in 0 until 8) {
                if (isValidMove(baslangicSatir, baslangicSutun, row, col, tas)) {
                    val targetSquare = chessboardGridLayout.getChildAt(row * 8 + col) as ImageView

                    // Enhanced valid move highlighting
                    targetSquare.setBackgroundResource(R.drawable.chess_square_valid_move)

                    // Add subtle animation
                    val alphaAnimation = ObjectAnimator.ofFloat(targetSquare, "alpha", 0.7f, 1.0f, 0.7f)
                    alphaAnimation.duration = 1000
                    alphaAnimation.repeatCount = ObjectAnimator.INFINITE
                    alphaAnimation.start()

                    vurgulananKareler.add(targetSquare)
                }
            }
        }
    }

    private fun temizleVurgulamalar() {
        val chessboardGridLayout = binding.chessboardGridLayout
        for (square in vurgulananKareler) {
            val index = chessboardGridLayout.indexOfChild(square)
            if (index != -1) {
                val row = index / 8
                val col = index % 8
                square.setBackgroundResource(getSquareOriginalDrawable(row, col))
                square.clearAnimation()
                square.alpha = 1.0f
            }
        }
        vurgulananKareler.clear()

        if (secilenTas != null) {
            if (secilenSatir != -1 && secilenSutun != -1) {
                if (!vurgulananKareler.contains(secilenTas)) {
                    secilenTas!!.setBackgroundResource(getSquareOriginalDrawable(secilenSatir, secilenSutun))
                }
            }
        }
    }

    private fun getSquareOriginalColor(row: Int, col: Int): Int {
        return if ((row + col) % 2 == 0) {
            Color.parseColor("#F0E68C")
        } else {
            Color.parseColor("#D2691E")
        }
    }

    private fun getSquareOriginalDrawable(row: Int, col: Int): Int {
        return if ((row + col) % 2 == 0) {
            R.drawable.chess_square_light
        } else {
            R.drawable.chess_square_dark
        }
    }
}