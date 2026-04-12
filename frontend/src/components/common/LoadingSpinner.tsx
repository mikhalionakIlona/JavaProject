import React from 'react';
import { motion } from 'framer-motion';

const LoadingSpinner: React.FC = () => {
    return (
        <div className="flex flex-col items-center justify-center min-h-[400px]">
            <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                className="w-12 h-12 border-4 border-purple-500/30 border-t-purple-500 rounded-full"
            />
            <motion.p
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.5 }}
                className="mt-4 text-white/60 font-medium"
            >
                Загрузка...
            </motion.p>
        </div>
    );
};

export default LoadingSpinner;